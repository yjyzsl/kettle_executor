#!/home/zeus/kettle_task_executor

#需要执行转换、JOB的目录
path=/个人工作目录/梁鄂湘

#需要执行转换、JOB的名称
name=Carte环境测试

#执行的类型：转换为ktr，JOB为kjb
type=kjb

#执行模式:remote和local，所有都是用remote执行
exec_mode=remote

#将导出的文件发送到远程服务器执行
pass_remote_server=1

#日志级别：Nothing(不记录日志)、Error（错误级别）、Minimal（最小输出）、Basic（基础）、Detailed（详细）、Debug（调试）、Rowlevel（行级别）
log_level=Basic

#是否启用安全模式执行：0否，1是
#enable_safe_mode=0

#执行前清除日志：0否，1是
#clean_log_befor_exec=0

#重放时间
#replay_date=yyyy/MM/dd hh:mm:ss

#对应kettle启动中的命名参数
parameters.val_1=xxxx
parameters.val_2=xxxx
parameters.val_3=xxxx

#对应kettle启动中的变量
variables.val_1=xxxx
variables.val_2=xxxx

#argument可以定义多个，最多不能超过10个，每行一个，从0开始计数，下面表示定义的Argument中0=a，1=b
arguments=a
arguments=b

