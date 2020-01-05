
## 第二章：linux环境准备与设置 
date: 2018-12-19 21:52:11



### 环境简介
利用VMware虚拟机+centos完成，基本要求笔记本电脑内存在8G以上。
最低要去克隆出3台虚拟机，每台给2G内存。
![](http://ww1.sinaimg.cn/large/005BOtkIly1fycdbmkr58j30m20ckq81.jpg)
### linux配置要点

**1）设置ip地址**
项目视频里面直接使用界面修改ip比较方便，如果Linux没有安装操作界面，需要使用命令：vi /etc/sysconfig/network-scripts/ifcfg-eth0 来修改ip地址，然后重启网络服务service network restart即可。 参考链接：[请点击。][1]

**2）创建用户**
大数据项目开发中，一般不直接使用root用户，需要我们创建新的用户来操作，比如kfk。
a）创建用户命令：adduser kfk
b）设置用户密码命令：passwd kfk

**3）文件中设置主机名**
Linux系统的主机名默认是localhost，显然不方便后面集群的操作，我们需要手动修改Linux系统的主机名。
a）查看主机名命令：hostname
b）修改主机名称
vi /etc/sysconfig/network
NETWORKING=yes
HOSTNAME=bigdata-pro01.kfk.com

**4）主机名映射**
如果想通过主机名访问Linux系统，还需要配置主机名跟ip地址之间的映射关系。
vi /etc/hosts
192.168.31.151 bigdata-pro01.kfk.com
配置完成之后，reboot重启Linux系统即可。
如果需要在windows也能通过hostname访问Linux系统，也需要在windows下的hosts文件中配置主机名称与ip之间的映射关系。在windows系统下找到C:\WINDOWS\system32\drivers\etc\路径，打开HOSTS文件添加如下内容：
192.168.31.151 bigdata-pro01.kfk.com

**5）root用户下设置无密码用户切换**
在Linux系统中操作是，kfk用户经常需要操作root用户权限下的文件，但是访问权限受限或者需要输入密码。修改/etc/sudoers这个文件添加如下代码，即可实现无密码用户切换操作。
vi /etc/sudoers
。。。添加如下内容即可
kfk ALL=(root)NOPASSWD:ALL

**6）关闭防火墙**
我们都知道防火墙对我们的服务器是进行一种保护，但是有时候防火墙也会给我们带来很大的麻烦。 比如它会妨碍hadoop集群间的相互通信，所以我们需要关闭防火墙。 那么我们永久关闭防火墙的方法如下:
vi /etc/sysconfig/selinux
SELINUX=disabled
保存、重启后，验证机器的防火墙是否已经关闭。
a）查看防火墙状态：service iptables status
b）打开防火墙：service iptables start
c）关闭防火墙：service iptables stop

**7）卸载Linux本身自带的jdk**
一般情况下jdk需要我们手动安装兼容的版本，此时Linux自带的jdk需要手动删除掉，具体操作如下所示：
a）查看Linux自带的jdk
rpm -qa|grep java 
b）删除Linux自带的jdk
rpm -e --nodeps [jdk进程名称1 jdk进程名称2 ...]
### 克隆虚拟机并进行相关的配置
前面我们已经做好了Linux的系统常规设置，接下来需要克隆虚拟机并进行相关的配置。
**1）kfk用户下创建我们将要使用的各个目录**
```css
软件目录
mkdir /opt/softwares
模块目录
mkdir /opt/modules
工具目录
mkdir /opt/tools
数据目录
mkdir /opt/datas
```
**2）jdk安装(1.7以上，1.9以下)**
大数据平台运行环境依赖JVM，所以我们需要提前安装和配置好jdk。 前面我们已经安装了64位的centos系统，所以我们的jdk也需要安装64位的，与之相匹配
下面步骤给的是1.7的。我自己用的是jdk1.8.0_191
a）将jdk安装包通过工具上传到/opt/softwares目录下
b）解压jdk安装包

    #解压命令
    tar -zxf jdk-7u67-linux-x64.tar.gz /opt/modules/
    #查看解压结果
    ls
    jdk1.7.0_67

c）配置Java 环境变量

    vi /etc/profile
    export JAVA_HOME=/opt/modules/jdk1.7.0_67
    export PATH=$PATH:$JAVA_HOME/bin

d）查看Java是否安装成功

    java -version
    java version "1.7.0_67"
    Java(TM) SE Runtime Environment (build 1.7.0_67-b15)
    Java HotSpot(TM) 64-Bit Server VM (build 24.79-b02, mixed mode)

**3）克隆虚拟机**

在克隆虚拟机之前，需要关闭虚拟机，然后右键选中虚拟机——》选择管理——》选择克隆——》选择下一步——》选择下一步——》选择创建完整克隆，下一步——》选择克隆虚拟机位置（提前创建好），修改虚拟机名称为Hadoop-Linux-pro-2，然后选择完成即可。
然后使用同样的方式创建第三个虚拟机Hadoop-Linux-pro-3。

**4）修改克隆虚拟机配置**
克隆完虚拟机Hadoop-Linux-pro-2和Hadoop-Linux-pro-3之后，可以按照Hadoop-Linux-pro-1的方式配置好ip地址、hostname，以及ip地址与hostname之间的关系。[参考链接][2]


  [1]: https://www.willxu.xyz/2018/08/23/hadoop/1%E3%80%81vmware%E4%B8%8A%E7%BD%91%E9%85%8D%E7%BD%AE/
  [2]: https://www.willxu.xyz/2018/08/23/hadoop/1%E3%80%81vmware%E4%B8%8A%E7%BD%91%E9%85%8D%E7%BD%AE/