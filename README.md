# VPRO_ Project AWS Lift and Shift:

# Prerequisites

- JDK 11 
- Maven 3 
- MySQL 8

# Technologies 
- Spring MVC
- Spring Security
- Spring Data JPA
- Maven
- JSP
- Tomcat
- MySQL
- Memcached
- Rabbitmq
- ElasticSearch
# Database
Here,we used Mysql DB 
sql dump file:
- /src/main/resources/db_backup.sql
- db_backup.sql file is a mysql dump file.we have to import this dump to mysql db server
- ` mysql -u <user_name> -p accounts < db_backup.sql`


# Architecture 

1)	EC2 Instances
2)	ELB (Elastic Load Balancer)
3) 	AutoScaling Group
4)	Amazon S3 - for Artifacts of mavan
5) 	Amazon Certificate Manager (ACM)
6)	Route 53

<figure>
<img src="./AWS_Lift&Shift_project.png" alt="Architecture Diagram" />
<figcaption><b><p>Architecture Diagram</p></b></figcaption>  
</figure>

# FLOW OF EXECUTION

-   Login to AWS Account

-   Create Key Pairs

-   Create Security Groups

-   Launch EC2 Instances with User Data [Bash Scripts]

-   Update IP to name mapping in Route53

-   Build Application from Source Code

-   Upload to S3 Bucket 

-   Download Artifacts to Tomcat Ec2 Instance 

-   Setup ELB with HTTPS [Cert from Amazon Certificate Manager(ACM)]

-   Map ELB Endpoint to website name in Godaddy DNS

<br>

# 1) Create Key Pairs:
<br>

-   Login To AWS Account 

-   EC2 > Network & Security > Key Pairs

-   Create Key Pair > name > Key Pair Type > Private Key file Format

-   .pem(Windows), .ppk(Linux) > Create Key Pair

<br>

# 2) Create Security Groups:
<br>

-   Create 3 Security Groups:

    -   Load balancer SG
    -   Tomcat SG
    -   Backend SG

>   Load Balancer Security Group (ELB) will allow HTTP & HTTPS Traffic on Port 80 & 443 for both IPV4 & IPV6 allowed from anywhere

>   Tomcat Security Group listens on port 8080 on traffic coming from ELB. port 22 for allowing SSH from MyIP, Tomcat connects with 3 backend services:

-   MySQL(3306). Memcache(11211), RabbitMQ(5672)

>   Backend Security Group will allow above 3 port Traffic from Tomcat Security Group also port 22 for SSH from MyIP

<br>

>   ELB Security Group:

<br>

-   EC2 > Network & Security > Security Groups > Create security group

-   Security group name (vpro-elb-sg)

-   Edit inbound rules: 
    
    -   Type > HTTP > Anywhere IPV4
    -   Type > HTTP > Anywhere IPV6
    -   Type > HTTPS > Anywhere IPV4
    -   Type > HTTPS > Anywhere IPV6

-   Create Security group

<br>

>   Tomcat(App) Security Group:

<br>

-   EC2 > Network & Security > Security Groups > Create security group

-   Security group name (vpro-app-sg)

-   Edit inbound rules: 

    -   Type > Custom TCP > Port Range > 8080 > Source > vpro-elb-sg
    -   Type > SSH > Source > My IP
-   Create Security group

<br>

>   Backend Security Group:

<br>

-   EC2 > Network & Security > Security Groups > Create security group

-   Security group name (vpro-backend-sg)

-   Edit inbound rules:

    -   Type > MYSQL/Aurora > Source > vpro-app-sg
    -   Type > Custom TCP > Port Range > 11211 > Source > vpro-app-sg
    -   Type > Custom TCP > Port Range > 5672 > Source > vpro-app-sg
    -   Type > SSH > Source > My IP
    -   Type > All traffic > Source > vpro-app-sg
-   Create Security group

<br>

>   Note: After creating vpro-backend-sg security group, add one more inbound rule to allow All traffic from vpro-backend-sg itself.

<br>

# 3) Launch EC2 Instances with User Data [Bash Scripts]

<br>

-   We are going to Setup 4 EC2 Instances:

    -   <b> MySQL Instance</b>
    -   <b> Memcache Instance</b>
    -   <b> RabbitMQ Instance</b>
    -   <b> Tomcat Instance</b>

-   MySQL , MemCache, RabbitMQ Goes to <b>vpro-backend-sg</b> security group.

-   Tomcat Instance goes to <b>vpro-app-sg</b> security group.

<br>

>   MySQL Instance:

-   EC2 > Instances > Launch instances

    -   Name: vpro-db01
    -   AMI: Amazon Linux
    -   Amazon Linux(Free Tier)
    -   Instance Type: t2.micro
    -   Key pair (login) : Select the key pair name 
    -   Network settings > Firewall (security groups) > Select existing security group > Common security groups > <b>vpro-backend-sg</b>
    -   Advanced details > User data > paste the <b>mysql.sh</b> script file from userdata > Launch instance.

<br>

>   Memcache Instance:

-   EC2 > Instances > Launch instances

    -   Name: vpro-mc01
    -   AMI: Amazon Linux
    -   Amazon Linux(Free Tier)
    -   Instance Type: t2.micro
    -   Key pair (login) : Select the key pair name 
    -   Network settings > Firewall (security groups) > Select existing security group > Common security groups > <b>vpro-backend-sg</b>
    -   Advanced details > User data > paste the <b>memcache.sh</b> script file from userdata > Launch instance.

<br>

>   RabbitMQ Instance:

-   EC2 > Instances > Launch instances

    -   Name: vpro-rmq01
    -   AMI: Amazon Linux
    -   Amazon Linux(Free Tier)
    -   Instance Type: t2.micro
    -   Key pair (login) : Select the key pair name 
    -   Network settings > Firewall (security groups) > Select existing security group > Common security groups > <b>vpro-backend-sg</b>
    -   Advanced details > User data > paste the <b>rabbitmq.sh</b> script file from userdata > Launch instance.

<br>

>   Tomcat Instance:

-   EC2 > Instances > Launch instances

    -   Name: vpro-app01
    -   AMI: Ubuntu
    -   Ubuntu Server 24.04 LTS(Free Tier)
    -   Instance Type: t2.micro
    -   Key pair (login) : Select the key pair name 
    -   Network settings > Firewall (security groups) > Select existing security group > Common security groups > <b>vpro-app-sg</b>
    -   Advanced details > User data > paste the <b>tomcat_ubuntu.sh</b> script file from userdata > Launch instance.

<br>

> Services Verification in Instance:

-   EC2 > Instances > vpro-db01 > copy public IPv4 address

    -   open git bash/Terminal  
    -   `ssh -i /path/keypair.pem ec2-user@PublicIP`
    -   `systemctl status mariadb | grep active`
    -   `mysql -u root -padmin123 accounts;`

<br>

-   EC2 > Instances > vpro-mc01 > copy public IPv4 address

    -   open git bash/Terminal
    -   `ssh -i /path/keypair.pem ec2-user@PublicIP`
    -   `systemctl status memcached | grep active`

<br>

-   EC2 > Instances > vpro-rmq01 > copy public IPv4 address

    -   open git bash/Terminal
    -   `ssh -i /path/keypair.pem ec2-user@PublicIP`
    -   `systemctl status rabbitmq-server | grep active`

<br>