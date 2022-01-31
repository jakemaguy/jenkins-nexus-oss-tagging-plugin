# jenkins-nexus-oss-tagging

## Introduction

The goal of this plugin is to allow Jenkins Pipelines to create Artifact tags in the Nexus OSS edition.

There are plugins available to integrate with Pro version of Nexus, but OSS remains unsupported for tagging.

*Your Nexus instance requires the following plugin to be installed:*

https://github.com/sahabpardaz/nexus-tag-plugin

## Building and Installing the Plugin
Fetch dependencies with maven:

`mvn dependency:purge-local-repository`

Build and generate the HPI plugin file:

`mvn clean install`

Install the plugin by copying the generated HPI file to:

`JENKINS_HOME/plugins`

And restart Jenkins

## Jenkins Pipeline Example usage
For more detailed understanding of the tagging request body, see sonatype documentation on tagging in Nexus:

https://help.sonatype.com/repomanager3/nexus-repository-administration/tagging

```
nexusArtifactTagger(
    name: tagName, 
    nexusUrl: nexusServerUrl,
    attributes: [
        [key: 'Attribute-key', 
        value: 'Attribute-Value']
    ], 
    components: [
        [group: group, 
        name: name, 
        repo: repository, 
        version: version]
        ],
    ]
)
```

## Contributing

TODO review the default [CONTRIBUTING](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) file and make sure it is appropriate for your plugin, if not then add your own one adapted from the base file

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

