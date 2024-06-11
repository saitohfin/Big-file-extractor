# XML Tag Extractor

XML Tag Extractor is a Java application designed to extract information between XML tags from large files. It provides a flexible and efficient way to process XML data, making it ideal for dealing with big data files.

## Features

- **Tag Processing**: Extracts data between specified XML tags.
- **Large File Handling**: Efficiently processes large XML files without loading the entire file into memory.
- **Flexible Data Processing**: Allows for different ways of processing the extracted data, such as converting it to an object or writing it to an output stream.

## How to Use

1. **Define XML Tags**: Specify the XML tags you want to extract data from. You can do this by creating a list of tags and passing it to the `XmlTagProcessor`.

```java
final var one = "<one>";
final var second = "<second>";
final var third = "<third>";
final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));
```

2. **Process Extracted Data**: Define how you want to process the extracted data by implementing the `ValueProcessor` interface. You can create different implementations of `ValueProcessor` for different ways of processing the data.

```java
public class ObjectValueProcessor implements ValueProcessor {
    @Override
    public void processValue(String value) {
        // convert the value to an object and process it
    }
}
```

3. **Run the Extractor**: Run the `XmlTagExtractor` to start extracting and processing data.

```java
List<XmlTagProcessor> processors = tagPaths.stream()
    .map(tagPath -> new XmlTagProcessor(tagPath, new ObjectValueProcessor()))
    .toList();
```

## Requirements

- Java 8 or higher
- Maven

## Building the Project

To build the project, navigate to the project directory and run the following command:

```bash
mvn clean install
```

This will compile the code, run the tests, and package the application into a JAR file.

## Running the Application

To run the application, use the following command:

```bash
java -jar target/xml-tag-extractor-1.0.0.jar
```

Replace `xml-tag-extractor-1.0.0.jar` with the name of the JAR file generated during the build process.

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting a pull request.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.