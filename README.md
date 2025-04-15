# 功能

对有权限分级的系统进行越权测试的工具。

提供了

- 未授权检测

- 垂直越权检测
- 水平越权字段检测
- burp项目导入后进行批量检测

功能。



# 使用方法

通常情况下，使用者拥有一个系统的两个账号，一个高权限账号，一个低权限账号。

登录任意权限账号，判断系统的**鉴权字段**，通常为以下三种：

- Cookie
- http请求头

- 某个字段值，如是get请求的session字段：`/?session=xxxxxxxxxxxxx`

该系统提供两个选项，起码要设置其中一个：

- SetLowAuth

  ![image-20250414170709645](README.assets/image-20250414170709645.png)

- 字符替换

  ![image-20250414170752648](README.assets/image-20250414170752648.png)

SetLowAuth的优先级>字符替换

## 基础越权测试：SetLowAuth

SetLowAuth通过遍历请求包的参数，判断参数名是否匹配选中字符串，如匹配则将参数加入维护的Map中。在进行权限测试时，会便利Map中的参数，替换高权限请求包中的同名参数，进行越权测试。移除高权限请求包中的同名参数，进行未授权测试。且对Cookie请求头做了特殊判断，当选择字符串等于Cookie字符串时，会将Cookie请求头下的全部参数加入维护Map中，以待替换。

已www.baidu.com为例子，对于以下请求包

![image-20250414165322918](README.assets/image-20250414165322918.png)

如果鉴权字段是Cookie。登录低权限账号，选中Cookie，右键选择 Extension->JCheck->SetLowAuth

![image-20250414165418688](README.assets/image-20250414165418688.png)

在 JCheck 面板设置目标为：www.baidu.com  （ 只要目标Host字段包含该字符串均会进行检测）

![image-20250414165623307](README.assets/image-20250414165623307.png)

则，对于一个高权限请求（Cookie: BAIDUID=99999EB41151A76DA68B779C05DBF3AD;），如下

![image-20250414170134810](README.assets/image-20250414170134810.png)

点击Send，可以看到 JCheck 面包出现了对应的请求日志

![image-20250414170200073](README.assets/image-20250414170200073.png)

又因为高权限、低权限以及没有Cookie字段时返回的信息基本一致，所以判断路径`/sugrec`存在垂直越权和未授权，所以LowAuth和UnAuth字段为True

![](README.assets/image-20250414170246141.png)

![image-20250414170406129](README.assets/image-20250414170406129.png)

同时水平越权一栏会显示可能存在水平越权的参数



## 基础越权测试：字符替换

该功能最为简单，JCheck会将把请求书包中**低权限一栏的值**替换成高权限一栏的值。但是该功能的优先级小于SetLowAuth功能。

以路径为/pcag的请求包为例：

![image-20250414171147805](README.assets/image-20250414171147805.png)

如果我们想替换`Sec-Fetch-Dest: empty`的empty，则填入

![image-20250414171231048](README.assets/image-20250414171231048.png)

点击Send，可以看到日志捕获到了数据

![image-20250414171411256](README.assets/image-20250414171411256.png)

![image-20250414171426795](README.assets/image-20250414171426795.png)

![image-20250414171437206](README.assets/image-20250414171437206.png)

可以看到，对于待检测路径 `/pcag`，高权限的不变，低权限的为full，未授权的为空。

又因为高权限、低权限以及没有Cookie字段时返回的信息基本一致，所以判断为存在垂直越权和未授权，所以LowAuth和UnAuth字段为True

# 基础用法：鉴权字段是自定义请求头

对于以下数据包：

![image-20250414171905590](README.assets/image-20250414171905590.png)

假设其鉴权字段是Referer，则登录低权限账号，

![image-20250414171944875](README.assets/image-20250414171944875.png)

同样，选择Referer，设置为SetLowAuth。

则对于一个高权限账号的请求包（Referer：asdfa）：

![image-20250414172224065](README.assets/image-20250414172224065.png)

点击Send，可以看到 JCheck 面包捕获到了数据

![image-20250414172249652](README.assets/image-20250414172249652.png)

![image-20250414172318623](README.assets/image-20250414172318623.png)

![image-20250414172327552](README.assets/image-20250414172327552.png)![image-20250414172327875](README.assets/image-20250414172327875.png)

可以看到，对于待检测路径 `/`，高权限账号认证字段Refere不变，低权限账号的权限为其自己，未授权没有该字段。

又因为高权限、低权限以及没有Cookie字段时返回的信息基本一致，所以判断为存在垂直越权和未授权，所以LowAuth和UnAuth字段为True

# 进阶用法：BatchCheck

该功能一般使用于特殊情况。如通过burpproject导入数据流量，并设置SetLowAuth和字符替换后，直接对请求包进行批量检测。（注代码暂未优化，如某请求长时间无法响应，会卡死进程，导致检测时间变长。卡死的时间决定于BurpSuite请求的超时时间）

![image-20250415150711511](README.assets/image-20250415150711511.png)



![image-20250415150741408](README.assets/image-20250415150741408.png)

# 其他功能

**导出功能：**点击Export

![image-20250415151009690](README.assets/image-20250415151009690.png)

可在Extensions中看到结果输出：

![image-20250415151025672](README.assets/image-20250415151025672.png)



**清空功能：**点击Clear，会将系统重置到默认状态

![image-20250415151107808](README.assets/image-20250415151107808.png)

由于并发请求操作的原因，需要关闭Jcheck，并多次点击Clear

![image-20250415151144602](README.assets/image-20250415151144602.png)

