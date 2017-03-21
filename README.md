# docker-maven-plugin

## Purpose
Value addon for spotify docker plugin:
- Multiple endpoints support.
- Avoid 127 layer limitation when copying dependencies in AUFS
 
## Example
```xml
<plugin>
     <groupId>com.dy.docker.maven</groupId>
     <artifactId>docker-maven-plugin</artifactId>
     <version>1.0-SNAPSHOT</version>
     <executions>
         <execution>
             <phase>package</phase>
             <goals>
                 <goal>build</goal>
                 <goal>exec</goal>
             </goals>
             <configuration>
                 <hosts>localhost,192.168.1.255</hosts>
                 <bindedPorts>8080,8081</bindedPorts>
                 <baseImage>nimmis/java:openjdk-8-jre</baseImage>
                 <imageName>sampleImage</imageName>
                 <executor>${project.build.finalName}.jar</executor>
                 <main>com.foo.bar</main>
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
     </executions>           
</plugin>
```
