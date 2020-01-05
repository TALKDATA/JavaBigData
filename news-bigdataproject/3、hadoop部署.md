## 第三章：Hadoop2.X分布式集群部署
date: 2018-12-19 22:52:11


### 集群资源规划

利用VMware虚拟机+centos完成，基本要求笔记本电脑内存在8G以上。
最低要去克隆出3台虚拟机，每台给2G内存。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fycdbmkr58j30m20ckq81.jpg)

### 配置要点

**（一）hadoop2.x版本下载及安装**
官网下载2.x版本就好

**（二）hadoop配置要点**
参考官网给的例子：http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html
网站左下角有全部配置信息
**1）hadoop2.x分布式集群配置-HDFS**  
安装hdfs需要修改4个配置文件：hadoop-env.sh、core-site.xml、hdfs-site.xml和slaves
**2）hadoop2.x分布式集群配置-YARN**
安装yarn需要修改4个配置文件：yarn-env.sh、mapred-env.sh、yarn-site.xml和mapred-site.xml

**（三）分发配置到节点**
最好先SCP设置成无密码访问，需要生成秘钥，自己百度吧
hadoop相关配置在第一个节点配置好之后，可以通过脚本命令分发给另外两个节点即可，具体操作如下所示。
将安装包分发给第二个节点
scp -r hadoop-2.5.0 kaf@bigdata-pro02.kfk.com:/opt/modules/
将安装包分发给第三个节点
scp -r hadoop-2.5.0 kaf@bigdata-pro02.kfk.com:/opt/modules/

**（四）HDFS启动集群运行测试**
hdfs相关配置好之后，可以启动hdfs集群。
1.格式化NameNode
通过命令：bin/hdfs namenode -format 格式化NameNode。
2.启动各个节点机器服务
1）启动NameNode命令：sbin/hadoop-daemon.sh start namenode
2) 启动DataNode命令：sbin/hadoop-daemon.sh start datanode
3）启动ResourceManager命令：sbin/yarn-daemon.sh start resourcemanager
4）启动NodeManager命令：sbin/yarn-daemon.sh start resourcemanager
5）启动log日志命令：sbin/mr-jobhistory-daemon.sh start historyserver

**（五）YARN集群运行MapReduce程序测试**
前面hdfs和yarn都启动起来之后，可以通过运行WordCount程序检测一下集群是否能run起来。
集群自带的WordCount程序执行命令：bin/yarn jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.5.0.jar wordcount input output

**（六）ssh无秘钥登录** （可以提前设置好）
在集群搭建的过程中，需要不同节点分发文件，那么节点间分发文件每次都需要输入密码，比较麻烦。另外在hadoop 集群启动过程中，也需要使用批量脚本统一启动各个节点服务，此时也需要节点之间实现无秘钥登录。具体操作步骤如下所示：
1.主节点上创建 .ssh 目录，然后生成公钥文件id_rsa.pub和私钥文件id_rsa
mkdir .ssh
ssh-keygen -t rsa
2.拷贝公钥到各个机器
ssh-copy-id bigdata-pro1.kfk.com
ssh-copy-id bigdata-pro2.kfk.com
ssh-copy-id bigdata-pro3.kfk.com
3.测试ssh连接
ssh bigdata-pro1.kfk.com
ssh bigdata-pro2.kfk.com
ssh bigdata-pro3.kfk.com
4.测试hdfs
ssh无秘钥登录做好之后，可以在主节点通过一键启动命令，启动hdfs各个节点的服务，具体操作如下所示：
sbin/start-dfs.sh
如果yarn和hdfs主节点共用，配置一个节点即可。否则，yarn也需要单独配置ssh无秘钥登录。

**（七）配置集群内机器时间同步（使用Linux ntp进行）**
选择一台机器作为时间服务器，比如bigdata-pro1.kfk.com节点。
1.查看ntp服务是否已经存在
sudo rpm -qa|grep ntp
2.ntp服务相关操作
1）查看ntp状态
sudo service ntpd status
2）启动ntp
sudo service ntpd start
3）关闭ntp
sudo service ntpd stop
3.设置ntp随机器启动
sudo chkconfig ntpd on
4.修改ntp配置文件
vi /etc/ntp.conf
释放注释并将ip地址修改为
restrict 192.168.31.151 mask 255.255.255.0 nomodify notrap
注释掉以下命令行
server 0.centos.pool.ntp.org iburst
server 1.centos.pool.ntp.org iburst
server 2.centos.pool.ntp.org iburst
server 3.centos.pool.ntp.org iburst
释放以下命令行
server 127.127.1.0 #local clock
fudge 127.127.1.0 stratum 10
重启ntp服务
sudo service ntpd restart
5.修改服务器时间

    #设置当前日期
    sudo date -s 2017-06-16
    #设置当前时间
    sudo date -s 22:06:00

6.其他节点手动同步主服务器时间

    #查看ntp位置
    which ntpdate
    /usr/sbin/ntpdate
    1）手动同步bigdata-pro2.kfk.com节点时间
    sudo /usr/sbin/ntpdate bigdata-pro2.kfk.com
    2）手动同步bigdata-pro3.kfk.com节点时间
    sudo /usr/sbin/ntpdate bigdata-pro3.kfk.com
    7.其他节点定时同步主服务器时间
    bigdata-pro2.kfk.com和bigdata-pro3.kfk.com节点分别切换到root用户， 通过crontab -e 命令，每10分钟同步一次主服务器节点的时间。
    crontab -e
    #定时，每隔10分钟同步bigdata-pro1.kfk.com服务器时间
    0-59/10 * * * *  /usr/sbin/ntpdate bigdata-pro1.kfk.com

