Schools utility
===
Utility for counting and searching CSV-formatted data from [Common Core of Data](https://nces.ed.gov/ccd/CCDLocaleCode.asp).

### Usage

```
Usage: school options_list
Subcommands: 
    count - Print counts
    search - Run specified query
    search-interactive - Enter search terms interactively (preprocesses only one time)
    nuke - Nukes any existing index and redoes the indexing

Options: 
    --data, -d [school_data.csv] -> Full path to the school data file, including name of the file. 
                                    If not set, looks for data file at school_data.csv { String }
    --query, -q -> Search query terms { String }
    --help, -h -> Usage info 
    
```

### Installation
The utility is distributed as a binary, `schools.zip`. The school data file is packaged 
with the binary and lives at `schools/school_data.csv`.

Pre-requisites are:
* Make sure you've got Java installed on your machine

To install and run:
```bash
// To install
# unzip schools.zip
# cd schools

// To run
# ./bin/schools count
# ./bin/schools search -q "jefferson belleville" 
```

### Source 

Important source files are

1. Count code in `schools/src/main/kotlin/count.kt`

2. Search code in `schools/src/main/kotlin/search.kt`

3. Unit tests at `schools/src/test`

Dependencies are listed in `build.gradle.kts` and are:

1. [kotlin-csv-jvm](https://github.com/doyaaaaaken/kotlin-csv)

2. [kotlinx-cli](https://github.com/Kotlin/kotlinx-cli)

