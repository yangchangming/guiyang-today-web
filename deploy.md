# 部署相关

## 概要

   基于开源社区symphony的本地GuiZhou IT社区(gzit.info)

## 说明

   1. 修改my.cnf，binlog_format=row

   2. 保持latke为开发模式 runtimeMode=DEVELOPMENT，否则资源文件加载时需要min等资源文件

   3. latke中修改serverPort=80

   4. init中修改管理员email，admin.email=neocode@126.com

   5. 执行mvn clean package， 拷贝target下的vns-ink-1.4.0.war文件至jetty/webapps/中，重命名为root.war

   6. 后台启动服务, nohup java -jar start.jar &

   修改头像注意的问题：不能上传png格式，否则无法读出图片，只能上传jpg格式图片。

   websocket配置，只要配置nginx就可以了
   

------------

开发情况说明：
1. 完成了微信登录的pc端逻辑
2. 完成了移动端微信登录的token机制逻辑，即生成和校验token
3. 剩下的开始写各个业务逻辑的API接口部分
4. 微信小程序端需要继续开发

