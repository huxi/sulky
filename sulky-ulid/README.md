# Universally Unique Lexicographically Sortable Identifier

A Java port of [alizain/ulid](https://github.com/alizain/ulid) with binary format implemented.

## Background

A GUID/UUID can be suboptimal for many use-cases because:

- It isn't the most character efficient way of encoding 128 bits
- It provides no other information than randomness

A ULID however:

- Is compatible with UUID/GUID's
- 1.21e+24 unique ULIDs per millisecond (1,208,925,819,614,629,174,706,176 to be exact)
- Lexicographically sortable
- Canonically encoded as a 26 character string, as opposed to the 36 character UUID
- Uses Crockford's base32 for better efficiency and readability (5 bits per character)
- Case insensitive
- No special characters (URL safe)

## Install

```shell
../gradlew build install
```

Executing the above command in this directory will build, test and install this module in the local Maven repository under the Maven coordinates `de.huxhorn.sulky:de.huxhorn.sulky.ulid:[version]`. The generated jar file is also available in the `build/lib` directory.

## Usage

Generating a ULID String requires a ULID instance.

```java
import de.huxhorn.sulky.ulid.ULID;

ULID ulid = new ULID();
```

The default constructor is using a `java.security.SecureRandom` but you can also use the `ULID(Random)` constructor to use a different `Random` implementation.
Creating a `Random` instance is a costly operation so you should try to re-use the `ULID` instance if you will generate multiple ULIDs.

```java
// generate a ULID string
// this is likely what you are looking for
String ulidString = ulid.nextULID();

// generate a ULID Value instance
ULID.Value ulidValue = ulid.nextValue();

// generate the byte[] for a ULID.Value
byte[] data = ulidValue.toBytes();

// generate a ULID.Value from given bytes using the static fromBytes method
ULID.Value ulidValueFromBytes = ULID.fromBytes(data);

// generate a ULID.Value from given String using the static parseULID method
ULID.Value ulidValueFromString = ULID.parseULID(ulidString);

// generate a ULID string from ULID.Value
String ulidStringFromValue = ulidValue.toString();
```


## Specification

Below is the current specification of ULID as implemented in this repository.

### Components

**Timestamp**
- 48 bits
- UNIX-time in milliseconds
- Won't run out of space till the year 10889 AD

**Entropy**
- 80 bits
- User defined entropy source.

### Encoding

[Crockford's Base32](http://www.crockford.com/wrmg/base32.html) is used as shown.
This alphabet excludes the letters I, L, O, and U to avoid confusion and abuse.

```
0123456789ABCDEFGHJKMNPQRSTVWXYZ
```

### Binary Layout and Byte Order

The components are encoded as 16 octets. Each component is encoded with the Most Significant Byte first (network byte order).

```
0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      32_bit_uint_time_high                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     16_bit_uint_time_low      |       16_bit_uint_random      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       32_bit_uint_random                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       32_bit_uint_random                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

### String Representation

```
 01AN4Z07BY      79KA1307SR9X4MV3
|----------|    |----------------|
 Timestamp           Entropy
  10 chars           16 chars
   48bits             80bits
   base32             base32
```


## Test
```shell
../gradlew test jacocoTestReport
```

Tests are executed automatically during build. The above command will also generate a coverage report available in  `build/reports/jacoco/test/html/index.html`.

## Benchmarks

```shell
../gradlew jmh
```
Benchmark result is saved to `build/reports/jmh/results.txt`.


On 2,3 GHz Intel Core i7, macOS 10.12.2 and Java 1.8.0_112:

```
Benchmark                                        Mode  Cnt       Score       Error  Units
ULIDBenchmark.ULIDnextULIDThroughput            thrpt   20  486506.412 ± 11302.599  ops/s
ULIDBenchmark.ULIDnextValuetoStringThroughput   thrpt   20  489951.984 ±  4538.516  ops/s
ULIDBenchmark.UUIDrandomUUIDtoStringThroughput  thrpt   20  491242.333 ±  7327.215  ops/s
ULIDBenchmark.ULIDnextULIDAverage                avgt   20    2053.440 ±    30.052  ns/op
ULIDBenchmark.ULIDnextValuetoStringAverage       avgt   20    2043.318 ±    37.893  ns/op
ULIDBenchmark.UUIDrandomUUIDtoStringAverage      avgt   20    2057.551 ±    30.591  ns/op
```

UUID above is included as a reference value and is executing `java.util.UUID.randomUUID().toString()`.

## Prior Art

- [alizain/ulid](https://github.com/alizain/ulid)
