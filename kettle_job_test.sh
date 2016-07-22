#!/home/zeus/kettle_executor/runner

#需要执行转换、JOB的目录
path=/个人工作目录/梁鄂湘

#需要执行转换、JOB的名称
name=Carte环境测试

#执行的类型：转换为ktr，JOB为kjb
type=kjb

#日志级别：Nothing(不记录日志)、Error（错误级别）、Minimal（最小输出）、Basic（基础）、Detailed（详细）、Debug（调试）、Rowlevel（行级别）
log_level=Basic

#对应kettle启动中的命名参数
parameters.val_1=abcd

#对应kettle启动中的变量
variables.interval=5

#argument可以定义多个，最多不能超过10个，每行一个，从0开始计数，下面表示定义的Argument中0=a，1=b
#arguments=a
#arguments=b

