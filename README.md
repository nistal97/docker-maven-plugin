# docker-maven-plugin

##Purpose
Build, Deploy and manage docker image to remote docker daemon. It is target for java app only for now.   
Comparing with popular maven plugins like spotify, this also provides:   
- Easy config with endpoints.
- Resource copy has no 120 layer limitation.
- Create/Start container by either native or docker-compose 
 
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
