This is a [Moodle](https://moodle.org/) instance running inside a Docker container.
It can be used to test the [Moodle REST API](https://docs.moodle.org/dev/Web_service_API_functions) and related functionality locally.

## Setup
1. Run the stack
2. Login as the admin\
   URL: [`http://localhost:21080`](http://localhost:21080)\
   Username: `user`\
   Password: `bitnami`
3. Create user\
   => Site administration -> Users -> Add a new user
4. Enable [web services](http://localhost:21080/admin/search.php?query=enablewebservices)
   and [REST protocol](http://localhost:21080/admin/settings.php?section=webserviceprotocols)
   and [Mobile App](http://localhost:21080/admin/webservice/service.php?id=1)
5. Enable required permissions\
   => Site administration -> Users -> Define roles -> click gear of `Authenticated user` ->
   enable `moodle/webservice:createmobiletoken` and `webservice/rest:use`
6. Create a new token\
   => `curl 'http://localhost:21080/login/token.php?username=<username>&password=<password>&service=moodle_mobile_app'`\
   Use the credentials of the student user. \
   The `token` field of the response is the `appSecret` required in the integration setup.

## Create a course
1. Create a course\
   => Site administration -> Courses -> Add a new course
2. Enroll the user in the course\
   => Course administration -> Users -> Enrolled users
3. Continue to create Assignments
