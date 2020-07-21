# Mkflow

Mkflow is cloud based application which can be used to provision the cloud compute for running CI/CD pipeline or run batch processing.
  - Cloud agnostic
  - Fully scalable
  
  
### How to deploy

##### Native Deploy to AWS Cloud
```
npm i # Installing all dependencies 
npm i -g serverless
mvn clean package -P lambda 
serverless deploy # Deploying to the AWS Cloud

```

##### Docker Deploy to AWS Cloud
```
create-docker.sh
```



 