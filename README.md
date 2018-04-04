### 作用
获取高德地图中国行政区信息

### 使用说明
* 获取高德Key，详情参考：https://lbs.amap.com/api/webservice/guide/create-project/get-key。  
* 将Key赋值至“vip/zhaotao/District.java:141”。 
* 进入项目执行“mvn clean package  && java -jar target/amap-1.0.0.jar”命令 。（Windows系统会在桌面生成相应的SQL文件，其他系统则在用户目录下。）