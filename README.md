# docker-maven-plugin

## Purpose
Value addon for spotify docker plugin:
- Multiple endpoints support.
- Lifecycle management
- Avoid 127 layer limitation when copying dependencies in AUFS
- provide dockerfile fragment to combine with configured baseImage

## Example
```xml
<plugin>
    <groupId>com.dy.docker.maven</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
            <execution>
                <id>build_image</id>
                <phase>package</phase>
                <goals>
                    <goal>build</goal>
                </goals>
                <configuration>
                    <bSkip>true</bSkip>
                    <hosts>localhost</hosts>
                    <baseImage>nimmis/java:openjdk-8-jre</baseImage>
                    <imageName>${project.artifactId}</imageName>
                    <dockerFileFragmentPath>${project.build.directory}/../src/main/resources/my_Dockerfile</dockerFileFragmentPath>
                    <executor>${project.build.finalName}.jar</executor>
                    <main>com.dy.mercury.MercuryPlatform</main>
                    <resources>
                        <resource>
                            <directory>${project.build.directory}</directory>
                            <include>${project.build.finalName}.jar</include>
                        </resource>
                        <resource>
                            <directory>${project.build.directory}/docker/lib</directory>
                        </resource>
                    </resources>
                </configuration>
            </execution>
            <execution>
                <id>deploy_remote</id>
                <phase>install</phase>
                <goals>
                    <goal>exec</goal>
                </goals>
                <configuration>
                    <bSkip>false</bSkip>
                    <hosts>localhost</hosts>
                    <imageName>${project.artifactId}</imageName>
                    <bindedPorts>7777</bindedPorts>
                    <containerId>31534e42cd48</containerId>
                    <doStartThen1Stop0>0</doStartThen1Stop0>
                </configuration>
            </execution>
    </executions>
</plugin>
```
