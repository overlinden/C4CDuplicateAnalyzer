[![Build Status](https://travis-ci.com/overlinden/C4CDuplicateAnalyzer.svg?branch=master)](https://travis-ci.com/overlinden/C4CDuplicateAnalyzer) 
[![codecov](https://codecov.io/gh/overlinden/C4CDuplicateAnalyzer/branch/master/graph/badge.svg)](https://codecov.io/gh/overlinden/C4CDuplicateAnalyzer)

# C4CDuplicateAnalyzer

C4CDuplicateAnalyzer is a mass duplicate finder for SAP Cloud for Customer based on Spring Batch. It is highly configurable and uses multithreading for a great performance.

## Installation

Make sure that you have [maven](https://maven.apache.org/download.cgi) and a proper Java IDE installed on your system.

Clone this git repository into your working directory

```bash
git clone https://github.com/overlinden/C4CDuplicateAnalyzer.git
```

Open the project in your IDE

Compile the application

## Usage

Create a application.properties with the following properties. Replace the placeholders with your C4C url, username and password.
For each job you want to execute, add one job definition block to your application.properties file. A job is defined by the country, a comma separated list of roles, a threshold and an output file name. 

Example:

```properties 
logging.level = INFO
logging.file = application.log
logging.level.de.wpsverlinden.c4caccountduplicate=INFO

c4cduplicateanalyzer.endpoint = https://myXXXXXX.crm.ondemand.com/sap/c4c/odata/v1/c4codataapi
c4cduplicateanalyzer.user = YOUR_USERNAME
c4cduplicateanalyzer.password = YOUR_PASSWORD

c4cduplicateanalyzer.chunksize = 10000

c4cduplicateanalyzer.jobs[0].countrycode = AT
c4cduplicateanalyzer.jobs[0].roles = Z01
c4cduplicateanalyzer.jobs[0].threshold = 0.75
c4cduplicateanalyzer.jobs[0].outputfilename = AT_075_Z01_Duplicates.txt

c4cduplicateanalyzer.jobs[1].countrycode = DE
c4cduplicateanalyzer.jobs[1].roles = Z02
c4cduplicateanalyzer.jobs[1].threshold = 0.85
c4cduplicateanalyzer.jobs[1].outputfilename = DE_085_Z02_Duplicates.txt
```


## How does it work?

The application will download all customers based on the defined country and roles from your C4C environment. After that it calculates the similarity (based on the [levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance)) between all customers based on several hardcoded fields. If the similarity exceeded the defined threshold, a potential duplicate is recognized and reported.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[GNU General Public License v3.0](https://github.com/overlinden/C4CDuplicateAnalyzer/blob/master/LICENSE)
