File f=new File('.');

File gitHome=findGitHome(f)

//println "GitHome: ${gitHome}"

if(gitHome)
{
	headHash=getHeadHash(gitHome)
	//println("HEAD hash: ${headHash}")
	println(headHash)
}

File findGitHome(File f)
{
	while(f)
	{
		f=f.absoluteFile
		File gitHome=new File(f, '.git');
		if(gitHome.directory)
		{
			return gitHome;
		}
		f=f.parentFile
	}
}

String getHeadHash(File gitHome)
{
	if(!gitHome)
	{
		return null
	}
	
	final REF_PREFIX='ref: ';
	try
	{
		String line;
		new File(gitHome, 'HEAD').withReader{
			r -> line=r.readLine()
		}
		
		if(!line || !line.startsWith(REF_PREFIX))
		{
			return null;
		}
		
		new File(gitHome, line.substring(REF_PREFIX.length())).withReader{
			r -> line=r.readLine()
		}
		return line
	}
	catch(IOException ex)
	{
		return null
	}
}
