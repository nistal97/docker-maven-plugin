# docker-maven-plugin

##Purpose
Build, Deploy and manage docker image to remote docker daemon. It is target for java app only for now.   
Comparing with popular maven plugins like spotify, this also supports:   
- Easy config with endpoints.
- Dependency management, auto generate manifest file with dependencies. 
- No layer limitation, copy resource per layer has 120 size limitation.
