/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Based on https://raw.githubusercontent.com/spring-projects/spring-framework/0c178ff7621fd951fd1349287b726fb4c69c9c34/buildSrc/src/main/groovy/org/springframework/build/gradle/DetectSplitPackagesPlugin.groovy
package foo

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


/**
 * Gradle plugin that detects identically named, non-empty packages split across multiple
 * subprojects, e.g. "org.springframework.context.annotation" existing in both spring-core
 * and spring-aspects. Adds a 'detectSplitPackages' task to the current project's task
 * collection. If the project already contains a 'check' task (i.e. is a typical Gradle
 * project with the "java" plugin applied), the 'check' task will be updated to depend on
 * the execution of 'detectSplitPackages'.
 *
 * By default, all subprojects will be scanned. Use the 'projectsToScan' task property to
 * modify this value. Example usage:
 *
 *     apply plugin: 'detect-split-packages // typically applied to root project
 *
 *     detectSplitPackages {
 *         packagesToScan -= project(":spring-xyz") // scan every project but spring-xyz
 *     }
 *
 * @author Rob Winch
 * @author Glyn Normington
 * @author Chris Beams
 */
public class DetectSplitPackagesPlugin implements Plugin<Project> {
	public void apply(Project project) {
		def tasks = project.tasks
		Task detectSplitPackages = tasks.create("detectSplitPackages", DetectSplitPackagesTask.class)
		/*
		if (tasks.asMap.containsKey("check")) {
			tasks.getByName("check").dependsOn detectSplitPackages
		}
		*/
	}
}

public class DetectSplitPackagesTask extends DefaultTask {

	private static final String JAVA_FILE_SUFFIX = ".java"
	private static final String HIDDEN_DIRECTORY_PREFIX = "."

	@Input
	Set<Project> projectsToScan = project.subprojects

	public DetectSplitPackagesTask() {
		this.group = "Verification"
		this.description = "Detects packages split across two or more subprojects."
	}

	@TaskAction
	public void detectSplitPackages() {
		def splitPackages = doDetectSplitPackages()
		if (!splitPackages.isEmpty()) {
			def message = "The following split package(s) have been detected:\n"
			splitPackages.each { pkg, mod ->
				message += " - ${pkg} (split across ${mod})\n"
			}
			throw new GradleException(message)
		}
		
		// only perform if no split packages are already fixed
		Map<Project, Set<String>> packagesByProject = mapPackagesByProject()
		Map<Project, Set<List<String>>> packageStepsByProject = producePackageStepsByProject(packagesByProject)
		Map<Project, String> projectModuleNames = new HashMap<>()
		Map<String, Set<Project>> moduleNameProjects = new HashMap<>()
		
		boolean correct = true
		packageStepsByProject.each { project, packageSteps ->
			List<String> commonSteps = PackageHelper.findCommonPrefix(packageSteps)
			String commonName = PackageHelper.toPackageName(commonSteps)
			projectModuleNames.put(project, commonName)
			Set<Project> projects = moduleNameProjects.get(commonName)
			if(projects == null) {
				projects = new HashSet<>()
				moduleNameProjects.put(commonName, projects)
			}
			projects.add(project)
			if(!commonName.equals(project.jar.manifest.getAttributes().get('Automatic-Module-Name'))) {
				correct = false
				if(!commonName.equals(project.archivesBaseName)) {
					println "!!! ${project}\t${project.archivesBaseName}\t${commonName}"
				} else {
					println "${project}\t${commonName}"
				}
			}
		}

		def message = "The following module names have been suggested for multiple projects:\n"
		boolean problemDetected = false
		moduleNameProjects.each { moduleName, projects ->
			if(projects.size() > 1) {
				message += " - ${moduleName} (suggested for ${projects})\n"
				problemDetected = true
			}
		}
		if(problemDetected) {
			throw new GradleException(message)
		}
		if(correct) {
			println 'Everything is fine.'
		}
	}
	
	private Map<String, Set<String>> doDetectSplitPackages() {
		def splitPackages = [:]
		Map<Project, Set<String>> packagesByProject = mapPackagesByProject()

		packagesByProject.each { project, packages ->
			packages.each { packageDirectory ->
				def packageName = packageDirectory.substring(1).replaceAll(File.separator, '.')
				def projectSet = splitPackages[packageName]
				if(!projectSet) {
					projectSet = new TreeSet<String>()
					splitPackages[packageName] = projectSet
				}
				projectSet.add(project.name)
			}
		}

		def iter = splitPackages.entrySet().iterator()
		while(iter.hasNext()) {
			def next = iter.next()
			if(next.value.size() < 2) {
				iter.remove()
			}
		}

		return splitPackages;
	}

	private static Map<Project, Set<List<String>>> producePackageStepsByProject(Map<Project, Set<String>> packagesByProject) {
		Map<Project, Set<List<String>>> result = new HashMap<>()
		
		packagesByProject.each { project, packages ->
			result.put(project, PackageHelper.packageDirsToPackageSteps(packages))
		}		

		return result
	}
	
	private Map<Project, Set<String>> mapPackagesByProject() {
		def packagesByProject = [:]
		this.projectsToScan.each { Project p ->
			def sourceSets = p.findProperty('sourceSets')
			if(sourceSets != null) {
			def packages = new HashSet<String>()
				sourceSets.main.java.srcDirs.each { File dir ->
					DetectSplitPackagesTask.findPackages(packages, dir, '')
				}
				if (!packages.isEmpty()) {
					packagesByProject.put(p, packages)
				}
			}
		}
		return packagesByProject;
	}

	private static void findPackages(Set<String> packages, File dir, String packagePath) {
		def scanDir = new File(dir, packagePath)
		def File[] javaFiles = scanDir.listFiles({ file ->
			!file.isDirectory() && file.name.endsWith(JAVA_FILE_SUFFIX)
		} as FileFilter)

		if (javaFiles != null && javaFiles.length != 0) {
			packages.add(packagePath)
		}

		scanDir.listFiles({ File file ->
			file.isDirectory() && !file.name.startsWith(HIDDEN_DIRECTORY_PREFIX)
		} as FileFilter).each { File subDir ->
			findPackages(packages, dir, packagePath + File.separator + subDir.name)
		}
	}
}


class PackageHelper {
	static {
		assert toPackageName(['com', 'foo', 'bar']) == 'com.foo.bar'
		assert toPackageName(['com']) == 'com'
		assert toPackageName([]) == ''
		
		assert minimumSize([] as Set) == 0
		assert minimumSize([[]]  as Set) == 0
		assert minimumSize([['foo']] as Set) == 1
		assert minimumSize([['foo'], ['bar']] as Set) == 1
		assert minimumSize([['foo'], ['foo', 'bar']] as Set) == 1
		
		assert packageDirToPackageSteps('') == []
		assert packageDirToPackageSteps('/') == []
		assert packageDirToPackageSteps('/a') == ['a']
		assert packageDirToPackageSteps('/a/b/') == ['a', 'b']
		
		assert packageDirsToPackageSteps([] as Set) == [] as Set
		assert packageDirsToPackageSteps(['foo'] as Set) == [['foo']] as Set
		assert packageDirsToPackageSteps(['foo/bar'] as Set) == [['foo', 'bar']] as Set
		assert packageDirsToPackageSteps(['foo/bar', '/bar/foo/'] as Set) == [['foo', 'bar'], ['bar', 'foo']] as Set
		
		assert findCommonPrefix([] as Set) == []
		assert findCommonPrefix([['']] as Set) == ['']
		assert findCommonPrefix([['a'], ['a']] as Set) == ['a']
		assert findCommonPrefix([['a', 'a'], ['a', 'b']] as Set) == ['a']
		assert findCommonPrefix([['a', 'b', 'c'], ['a', 'b', 'd']] as Set) == ['a', 'b']
	}
	
	public static String toPackageName(List<String> input) {
		StringBuilder result = new StringBuilder()
		boolean first = true
		input.each { current ->
			if(first) {
				first = false
			} else {
				result.append('.')
			}
			result.append(current)
		}
		return result.toString()
	}

	public static int minimumSize(Set<List<?>> input) {
		if(input.isEmpty()) {
			return 0
		}
		
		int result = Integer.MAX_VALUE
		input.each { current ->
			int currentSize = current.size()
			if(currentSize < result) {
				result = currentSize
			}
		}
		return result
	}

	public static List<String> packageDirToPackageSteps(String input) {
		StringTokenizer tokenizer = new StringTokenizer(input, '/', false)
		
		List<String> result = new ArrayList<>()
		while(tokenizer.hasMoreTokens()) {
			result.add(tokenizer.nextToken())
		}
		
		return result
	}

	public static Set<List<String>> packageDirsToPackageSteps(Set<String> input) {
		Set<List<String>> result = new HashSet<>()
		
		input.each { current ->
			List<String> steps = PackageHelper.packageDirToPackageSteps(current) 
			result.add(steps)
		}
		
		return result
	}
	
	private static final String MISMATCH_IDENTIFIER = '#### NOPE ####'
	
	public static List<String> findCommonPrefix(Set<List<String>> input) {
		int upperBounds = minimumSize(input)
		List<String> result = new ArrayList<>()
		if(upperBounds == 0 || input.isEmpty()) {
			return result
		}
		
		for(int i=0 ; i<upperBounds ; i++) {
			String previousLevelValue = null
			
			input.each { currentList ->
				String currentLevelValue = currentList[i]
				if(previousLevelValue == null) {
					previousLevelValue = currentLevelValue
				} else if(previousLevelValue != currentLevelValue) {
					previousLevelValue = MISMATCH_IDENTIFIER
					// return inside each closure is not return
					// REMEMBER THIS, DAMNIT!!
				}
			}
			
			if(MISMATCH_IDENTIFIER == previousLevelValue) {
				return result
			}
			
			result.add(previousLevelValue)
		}
		
		return result
	}	
}
