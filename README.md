# Mkflow

Mkflow is cloud based application which can be used to provision the cloud compute for running CI/CD pipeline or run batch processing.
  - Cloud agnostic
  - Fully scalable
  
  
### How to deploy

##### Native Deploy to AWS Serverless
```
npm i # Installing all dependencies 
npm i -g serverless
mvn clean package -P lambda 
serverless deploy # Deploying to the AWS Cloud

```

#####  Deploy to Docker 
```
mvn clean install
sudo ./build-docker.sh
docker exec -e AWS_ACCESS_KEY_ID=<AWS_ACCESS_KEY_ID> -e AWS_SECRET_ACCESS_KEY=<AWS_SECRET_ACCESS_KEY> mkflow
```


### Try it out

##### Run Commands
After the deployment you can give a simple commands to try out.

```POST /api/run```
```json
{
    "cloud":{
        "vendor": "aws",
        "provision":{
            "type":"market",
            "instanceType":["t2.small","t3.small"],
            "region":"ap-southeast-1"
        }
    },
   
    "container":{
        "auth":{
            "username":"ec2-user",
            "type":"key"
        }
    },

     "build": {
        "commands" : [
            "ls -al ",
            "uname -a "
        ]
    }
}
```

which must give you some output like :
```json
{
    "jobId": "c9d01a54-c21c-4ca4-b1e7-b3426596e387",
    "codebase": "/tmp/c9d01a54-c21c-4ca4-b1e7-b3426596e387/codebase"
}
```

##### View Logs
Open `<PUBLIC_URL>/log` in your browser where you need to put `jobId` to display all the logs.


### Supported
- Github, Gogs Hooks
- AWS Cloud 
- Gogs Hooks
- Provisioning using Spot instances only 


### Todo
- Bitbucket, Gitlab Hooks
- Azure, Google Cloud provisioning
- Slack integration 