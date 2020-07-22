# Mkflow
[![N|Solid](https://i.imgur.com/wWt2ADv.png)](https://quarkus.io/)

Mkflow is cloud based application which can be used to provision the cloud compute for running CI/CD pipeline or run batch processing.
  - Cloud agnostic
  - Fully scalable
  
### mkflow.yml
The application will look for `mkflow.yml` and other yaml for running a set of commands. 

This file contains all the server specification for provisioning an instance in a cloud.

- cloud
    - vendor  ["aws" | "azure" | "google"] -> Type of cloud vendor 
    - auth -> (Type of authentication to use) 
        - type [ "key" ] -> Automatically create , assign SSH Key and destory. 
    - provision
        - type ["market", "demand", "premises" ] -> Only market works for now
        - instanceType : ["t2.micro", "t3.large" ... ] -> Vendor specific instance type.
        - region -> Vendor specific region where the instance should be running
        - permission -> Vendor specific IAM permission to set permission for instance.
- container
    - image -> Vendor specific Operating system image name to run. default: ami image  
    - auth  
        - username -> username to use for logging through ssh protocol default : ec2-user 
- build  
    - commands - Set of commands to run in the provisioned instance. Supports relative uri as well if used single entry.
    - repo 
        - uri - The uri to clone the repository for source code 
        - auth -> (Type of authentication to use) 
            - type [ "token" ] -> use github token for now
    - cache
        - type - ["s3"] right now only saves the cache to S3 bucket
        - location - Specific location of S3 bucket eg. s3://<bucket>/<prefix>
___
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
sudo docker run -p 8080:8080 -e AWS_ACCESS_KEY_ID=<AWS_ACCESS_KEY_ID> -e AWS_SECRET_ACCESS_KEY=<AWS_SECRET_ACCESS_KEY> mkflow
```

___

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

___

### REST APIs

**/log [GET]**
> This is the html endpoint for watching logs of specific job by using jobId.


**/api/run [POST]**
> This api is used for running commands directly and this one does not have source repository at all. Normally this is used for job processing.
>  
> JSON Body: 
>```json
>{
>     "cloud":{
>         "vendor": "aws",
>         "provision":{
>             "type":"market",
>             "instanceType":["t2.small","t3.small"],
>             "region":"ap-southeast-1"
>         }
>     },
>    
>     "container":{
>         "auth":{
>             "username":"ec2-user",
>             "type":"key"
>         }
>     },
> 
>      "build": {
>         "commands" : [
>             "ls -al ",
>             "uname -a "
>         ]
>     }
> }
>```


**/api/hook?token=<GITHUB_TOKEN> [POST]**
> This is a webhook receiver api right now it only supports Github webhooks. 


___
### Supported
- Github, Gogs Hooks
- AWS Cloud 
- Gogs Hooks
- Provisioning using Spot instances only 

### Todo
- Bitbucket, Gitlab Hooks
- Azure, Google Cloud provisioning
- Slack integration 