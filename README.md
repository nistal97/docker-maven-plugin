# docker-maven-plugin

##Purpose
Docklize java app and deploy to remote docker daemon.
Comparing with spotify docker maven plugin, this also provides:
- Multiple endpoints.
- No AUFS 127 layer limitation.
- Alternative way for docker-compose
 
##Example
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
                 <hosts>localhost</hosts>
                 <bindedPorts>8080</bindedPorts>
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
