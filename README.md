# ODPS Eclipse plugin

This is an Eclipse plugin for developing [ODPS](http://www.aliyun.com/product/odps/) jobs

This plugin provides the features:
- Local Debug MapReduce program
- Local Debug UDF/UDTF program
- Local UDF/UDTF Unit Test

## Requirements

- Eclipse >= 3.7
- Java >= 1.6

## Usage

### Create ODPS project

- File -> New -> Project.. -> ODPS -> ODPS Project

    Mapper/Reducer/MapReducer Driver/UDF/UDTF template

- File -> New -> Other.. -> ODPS -> ..template

### run ODPS project

- Local Debug Mapreduce program

    Right-click class WordCount(or class Resource) ->  Run As -> ODPS Mapreduce -> Input parameters -> Finish

    Click Run in menu Bar -> Run Configurations -> Right-click ODPS Mapreduce -> New -> Input parameters -> Run

- Local Debug UDF/UDTF program

    Right-click class UDFExample(or class UDFResource) -> Run As -> ODPS UDF|UDTF -> Input parameters -> Finish

    Click Run in menu Bar -> Run Configurations -> Right-click ODPS ODPS UDF|UDTF -> New -> Input parameters -> Run

- Local UDF/UDTF Unit Test

    Right-click class UDFTest/UDTFTest -> Run As -> Java Application

## Precautions

- Each running mapper is isolated, so do not to share static variables between mappers.
- The running mapper and reducer are isolated, so do not share variables between them.

## Author

- [Yang Zhiyong](https://github.com/yangzhiyong1989)

## Licence

licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)
