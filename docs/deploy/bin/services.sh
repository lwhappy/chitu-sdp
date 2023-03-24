#!/bin/sh
#. /etc/profile
run_user="app"
base_dir="/app"
service_name=$2
dump_file=/app/logs/jvmdump/${service_name}.jvm.dump.`date +%Y%m%d%H%M%S`
jstack_file=/app/logs/jvmdump/${service_name}.jstack.`date +%Y%m%d%H%M%S`
service_path="${base_dir}/bigdata/${service_name}.jar"
service_pid="${base_dir}/bigdata/${service_name}.pid"
time_now=`date +%Y_%m_%d:%H:%M:%S`
self_dir=$(cd "$(dirname "$0")"; pwd)
server_port=$3
#spring boot 优雅停止，必须开启配置选项：endpoints.shutdown.enabled=true
spring_boot_shutdown=true
spring_boot_delete=true
spring_boot_down=false
spring_boot_out_of_service=true
eureka_url=""
. /etc/profile
export no_proxy="127.0.0.1, localhost"
log_write(){
        #cho `date "+%F %T"`:$1 >>$log
        echo `date "+%F %T"`:$1
}

############### for pinpoint agent id ##############
local_ip(){
  LOCAL_IP=`ifconfig | grep "inet [1-7]" | grep -v "127.0.0.1" | awk '{print $2}' | head -n 1`
  echo $LOCAL_IP|grep -E "([0-9]{1,3}[\.]){3}[0-9]{1,3}" > /dev/null
  if [ $? -ne 0 ];then
    echo "不合规的IP地址:$LOCAL_IP"
    exit 1
  fi
  hex_ip=`ip_to_hex ${LOCAL_IP:3}`
}
ip_to_hex() {
  printf '%02X' ${1//./ }
}
local_ip
############### for pinpoint agent id ##############
if [ "$JAVA_OPTS" == "" ];then
	JAVA_OPTS="-server -Xms512m -Xmx512m -XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCDateStamps   -Xloggc:$base_dir/logs/${service_name}_${server_port}.gclog"
fi
############### for jvm opstions ###################
if [ -f ${self_dir}/.gray-appjvm ];then
        if [ "$service_name" == "" ];then
                echo "参数错误:err_code 1"
                exit 1
        fi
        tmp=`cat ${self_dir}/.gray-appjvm | egrep -e  "^${service_name}," | tail -n 1 | egrep -e  "^${service_name},"`
        if [ $? -eq 0 ];then
                JAVA_OPTS=`echo $tmp | awk -F ',' '{print $2}'`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/HEX_IP_FLAG/$hex_ip/g"`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/SERVICE_NAME_FLAG/$service_name/g"`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/SERVICE_PORT_FLAG/$server_port/g"`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/COMMA_FLAG/,/g"`
        fi
        JAVA_OPTS=`echo ${JAVA_OPTS} "-XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCDateStamps   -Xloggc:$base_dir/logs/${service_name}_${server_port}.gclog"`

elif [ -f ${self_dir}/.appjvm ];then
        if [ "$service_name" == "" ];then
                echo "参数错误:err_code 1"
                exit 1
        fi
        tmp=`cat ${self_dir}/.appjvm | egrep -e  "^${service_name}," | tail -n 1 | egrep -e  "^${service_name},"`
        if [ $? -eq 0 ];then
                JAVA_OPTS=`echo $tmp | awk -F ',' '{print $2}'`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/HEX_IP_FLAG/$hex_ip/g"`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/SERVICE_NAME_FLAG/$service_name/g"`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/SERVICE_PORT_FLAG/$server_port/g"`
                JAVA_OPTS=`echo $JAVA_OPTS |sed "s/COMMA_FLAG/,/g"`
        fi
        JAVA_OPTS=`echo ${JAVA_OPTS} "-XX:+PrintGCDetails -XX:+PrintHeapAtGC -XX:+PrintGCDateStamps   -Xloggc:$base_dir/logs/${service_name}_${server_port}.gclog"`
fi
############### for jvm opstions ###################
if [ `whoami` != "app" ]; then
	echo "only app user can run !!!"
	exit 1
fi
get_instanceId(){
	instanceId=`curl -s --connect-timeout 2 -m 4  http://127.0.0.1:${server_port}/actuator/info | awk -F '"instanceId":"' '{print $2}' | awk -F '"' '{print $1}'`
}
spring_stop(){
	if [[ "${spring_boot_shutdown}" == "true" ]]
	then
		curl -s --connect-timeout 2 -m 4 -XPOST http://127.0.0.1:${server_port}/actuator/shutdown
		echo ''
	fi
}
spring_delete(){
	get_instanceId
	if [[ "${spring_boot_delete}" == "true" ]]
	then
		if [[ ${instanceId} != "" ]]
		then
			curl -s --connect-timeout 2 -m 2  -XDELETE ${eureka_url}${instanceId}
		fi
	fi
}
spring_down(){
	get_instanceId
	if [[ "${spring_boot_down}" == "true" ]]
	then
		if [[ ${instanceId} != "" ]]
		then
			curl -s --connect-timeout 2 -m 2  -XPUT ${eureka_url}${instanceId}/status?value=DOWN
		fi
	fi
}
spring_out_of_service(){
	Record_str="spring_out_of_service $service_name $server_port"
	create_Record $Record_str
	get_instanceId
    #非生产环境不进行要求强制下线
    if [ $? -ne 0 ]
    then
    	return 0
    fi
	if [[ "${spring_boot_out_of_service}" == "true" ]]
	then
		if [[ ${instanceId} != "" ]]
		then
		  i=1
		  http_code=`curl -w %{http_code} -s --connect-timeout 2 -m 2 -XPUT ${eureka_url}${instanceId}/status?value=OUT_OF_SERVICE`
		  while [ "$http_code" != "200" ]
		  do
		    if [ $i -ge 5 ]
		    then
		    	log_write "spring_out_of_service fail."
		    	hostname  | egrep 'prd|prod' > /dev/null
		        if [ $? -ne 0 ]
				then
					return 0
				else
					exit 1
				fi
		    fi
		    sleep 1s
		    let i=$i+1
		    http_code=`curl -w %{http_code} -s --connect-timeout 2 -m 2 -XPUT ${eureka_url}${instanceId}/status?value=OUT_OF_SERVICE`
		  done
		else
      		log_write "instanceId  null."
	    	hostname  | egrep 'prd|prod' > /dev/null
	        if [ $? -ne 0 ]
			then
				return 0
			else
				#exit 1
				return 0
			fi
		fi
	fi
}

init(){
  mkdir -p /app/logs/jvmdump/
	tmp_var=`which java`
	if [ $? -ne 0 ];then
		echo "JDK缺失"
		exit 1
	else
		java_path=$tmp_var
	fi
	tmp_var=`cat /etc/passwd | egrep -e "^$run_user:"`
	if [ $? -ne 0 ];then
		echo "运行用户不存在: $run_user"
		exit 1
	fi
	tmp_var=`ls $base_dir`
        if [ $? -ne 0 ];then
                echo "运行目录不存在$base_dir"
                exit 1
        fi
        tmp_var=`ls ${base_dir}/sh`
        if [ $? -ne 0 ];then
                echo "脚本目录不存在/app/sh"
                exit 1
        fi
        tmp_var=`ls ${base_dir}/logs`
        if [ $? -ne 0 ];then
                echo "日志目录不存在${base_dir}/logs"
                exit 1
        fi
}
create_Record(){
        lock_file=`echo $* | sed 's/ //g'`
        lock_file=/tmp/`echo ${lock_file} | md5sum  | awk '{print $1}'`.lock
        touch $lock_file
}
get_Record_time(){
		spring_sleep=60
		lock_file=`echo $* | sed 's/ //g'`
		lock_file=/tmp/`echo ${lock_file} | md5sum  | awk '{print $1}'`.lock
        if [[ -f $lock_file ]];then
        		#rm $lock_file
				current=`date +%s`
				lockfile_time=`stat -c  %Z $lock_file`
				let mod_time=${current}-${lockfile_time}
				if [[ $mod_time -gt $spring_sleep ]]
				then
					log_write 'sleep_time=$spring_sleep'
					spring_out_of_service
					sleep_time=$spring_sleep
				else
					let sleep_time=$spring_sleep-${mod_time}
				fi
        else
        		spring_out_of_service
        		sleep_time=$spring_sleep
        fi
}
lock(){
		#set -x -v -h
        lock_file=`echo $* | sed 's/ //g'`
        lock_file=/tmp/`echo ${lock_file} | md5sum  | awk '{print $1}'`.lock
        if [[ -f $lock_file ]];then
        		#rm $lock_file
				current=`date +%s`
				lockfile_time=`stat -c  %Z $lock_file`
				let mod_time=${current}-${lockfile_time}
				if [[ $mod_time -gt 600 ]]
				then
					rm $lock_file
					return 0
				fi
                echo "应用锁存在:$lock_file"
                exit 1
        else
                touch $lock_file
        fi
        #set +x  +v +h
}
unlock(){
		#set -x -v -h
        lock_file=`echo $* | sed 's/ //g'`
        lock_file=/tmp/`echo ${lock_file} | md5sum  | awk '{print $1}'`.lock
        if [[ -f $lock_file ]];then
                rm $lock_file
        fi
        #set +x  +v +h
}
check_evn(){
	hostname  | egrep  '\-dev\-|\-local\-|\-uat\-|\-str\-|\-stg\-|\-sit\-' > /dev/null
	#hostname  | egrep  '\-dev\-|\-local\-|\-uat\-|\-sit\-' > /dev/null
	if [ $? -ne 0 ]
	then
		return 1
	else
		return 0
	fi
}
start(){
	init
	lock_str="start $service_name $server_port"
	trap "unlock $lock_str;exit 1" 2
	lock  $lock_str
	status $service_name call
	if [ $? -eq 0 ];then
		if [ "$2" == "call" ];then
			return 1
		else
			echo -e "已经运行：\e[1;31m$service_name\e[0m"
			unlock  $lock_str
			exit 1
		fi
	fi
	tmp_var=`ls $service_path 2> /dev/null`
	if [ $? -ne 0 ];then
		echo -e "未知应用：\e[1;31m$service_path\e[0m"
		unlock  $lock_str
		exit 1
	fi
	echo -e "正在启动：\e[1;31m$service_name\e[0m"
	if [ "$server_port" -gt 0 ] 2>/dev/null ;then
		nohup $java_path $JAVA_OPTS -jar $service_path  --server.port=${server_port} >> $base_dir/logs/$service_name"-${server_port}-start.out" 2>&1 &
	else
		nohup $java_path $JAVA_OPTS -jar $service_path  >> $base_dir/logs/$service_name"-start.out" 2>&1 &
	fi
	pid=$!
	for i in {1..360}; do
		sleep 1s
		tmp_var=`ps -ef | grep -v grep | awk '{print $2}'| egrep ^${pid}$`
		if [ $? -ne 0 ];then
			echo -e "启动失败：\e[1;31m$service_name\e[0m"
			unlock  $lock_str
			exit 1
		fi
		if [[ "${server_port}" == "" ]];then
			server_port=`lsof  -i -P |grep ${pid} | grep LISTEN | grep TCP | awk -F ':' '{print $2}' | awk '{print $1}' |sort`
			if [ "$server_port" != "" ];then
				curl -q	-s --connect-timeout 2 -m 4 http://127.0.0.1:${server_port} > /dev/null
				if [ $? -eq 0 ];then
					echo -e "启动完成：\e[1;31m$service_name\e[0m (pid:${pid})"
					unlock  $lock_str
					exit 0
				fi
				server_port=""
			fi
		else
			curl -q	-s --connect-timeout 2 -m 4 http://127.0.0.1:${server_port} > /dev/null
			if [ $? -eq 0 ];then
				echo -e "启动完成：\e[1;31m$service_name\e[0m (pid:${pid})"
				unlock  $lock_str
				exit 0
			fi
		fi
	done
	echo -e "启动失败：\e[1;31m$service_name\e[0m"
	unlock  $lock_str
	exit 1
}
stop(){
	lock_str="stop $service_name $server_port"
	trap "unlock $lock_str;exit 1" 2
	lock  $lock_str
	status $service_name call
	if [ $? -ne 0 ];then
	    if [ "$2" == "call" ];then
			unlock  $lock_str
			return 1
	    else
			echo -e "已经停止：\e[1;31m$service_name\e[0m"
			unlock  $lock_str
	        exit 0
	    fi
	fi
	echo -e "正在停止$server_port：\e[1;31m$service_name\e[0m"
	if [ "$server_port" -gt 0 ] 2>/dev/null ;then
		echo "$time_now:正在停止 端口：$server_port 应用：$service_name" >> ${self_dir}/stop.log
		check_evn
		if [ $? -ne 0 ]
		then
			Record_str="spring_out_of_service $service_name $server_port"
			get_Record_time $Record_str
			echo "等待服务下线,${sleep_time}s ... ..."
			sleep $sleep_time
		fi
		spring_stop
		ps -ef | grep -v grep | grep "$service_path" | egrep "$server_port\$" | awk '{print $2}' | xargs kill
		stat_num=1
		tmp_var=`ps -ef | grep -v grep | grep "$service_path" | egrep "$server_port\$"`
		while [ $? -eq 0 ];do
			if [ $stat_num -ge 60 ];then
				ps -ef | grep -v grep | grep "$service_path" |  egrep "$server_port\$" |awk '{print $2}' | xargs kill -9
				echo "$time_now:强制停止 端口：$server_port 应用：$service_name" >> ${self_dir}/stop.log
				spring_delete
			fi
			sleep 1s
			let stat_num=$stat_num+1
			tmp_var=`ps -ef | grep -v grep | grep "$service_path" | egrep "$server_port\$"`
		done
		spring_delete
		echo -e "已经停止$server_port：\e[1;31m$service_name\e[0m"
	else
		echo "$time_now:正在停止 应用：$service_name" >> ${self_dir}/stop.log
		ps -ef | grep -v grep | grep "$service_path" | awk '{print $2}' | xargs kill
		stat_num=1
		tmp_var=`ps -ef | grep -v grep | grep "$service_path"`
		while [ $? -eq 0 ];do
			if [ $stat_num -ge 60 ];then
				ps -ef | grep -v grep | grep "$service_path" | awk '{print $2}' | xargs kill -9
				echo "$time_now:强制停止 应用：$service_name" >> ${self_dir}/stop.log
			fi
			sleep 1s
			let stat_num=$stat_num+1
			tmp_var=`ps -ef | grep -v grep | grep "$service_path"`
		done
		echo -e "已经停止：\e[1;31m$service_name\e[0m"
	fi
	unlock  $lock_str
}
superstop(){
	status $service_name call
	if [ $? -ne 0 ];then
	    if [ "$2" == "call" ];then
			return 1
	    else
			echo -e "已经停止：\e[1;31m$service_name\e[0m"
	        exit 0
	    fi
	fi
	echo -e "正在停止$server_port：\e[1;31m$service_name\e[0m"
	if [ "$server_port" -gt 0 ] 2>/dev/null ;then
		instanceId=`curl -s --connect-timeout 2 -m 4  http://127.0.0.1:${server_port}/info | awk -F '"instanceId":"' '{print $2}' | awk -F '"' '{print $1}'`
		spring_delete
		ps -ef | grep -v grep | grep "$service_path" | egrep "$server_port\$" | awk '{print $2}' | xargs kill -9
		echo -e "已经停止$server_port：\e[1;31m$service_name\e[0m"
	else
		ps -ef | grep -v grep | grep "$service_path" | awk '{print $2}' | xargs kill -9
		echo -e "已经停止：\e[1;31m$service_name\e[0m"
	fi
}
status(){
	if [ "$server_port" -gt 0 ] 2>/dev/null ;then
		tmp_var=`ps -ef | grep -v grep | grep "$service_path" | egrep "$server_port\$"`
	else
		tmp_var=`ps -ef | grep -v grep | grep "$service_path"`
	fi
	if [ $? -eq 0 ];then
    	if [ "$2" == "call" ];then
			return 0
		else
			echo -e "已经运行：\e[1;31m$service_name\e[0m"
			return 0
        fi
	else
		if [ "$2" == "call" ];then
			return 1
		else
			tmp_var=`ls $service_path 2> /dev/null`
			if [ $? -ne 0 ];then
				echo -e "未知应用：\e[1;31m$service_path\e[0m"
				return 1
			fi
			echo -e "已经停止：\e[1;31m$service_name\e[0m"
			return 1
		fi
	fi
}
kill(){
	superstop $*
}
restart(){
	stop $service_name call
	start $service_name call
}
check_health(){
	if [ "$service_name" == "" -o "$server_port" == "" ]
	then
	        echo "参数缺失..."
	        exit 1
	fi
    i=0
    curl -q -s --connect-timeout 2 -m 4 http://127.0.0.1:${server_port}/info  > /dev/null
    while [[ $? -ne 0 ]]
    do
        sleep 1s
        let i=$i+1
        if [ $i -ge 3 ]
        then
    		echo "开始强杀服务:$service_name 端口:$server_port"
    		service_pid=`ps -ef | grep -v grep | grep "$service_path" |  egrep "$server_port\$" |awk '{print $2}'`
    		jstack ${service_pid} >${jstack_file}.01
    		jstack ${service_pid} >${jstack_file}.02
    		jstack ${service_pid} >${jstack_file}.03
    		jstack ${service_pid} >${jstack_file}.04
    		jstack ${service_pid} >${jstack_file}.05
    		echo "jmap -histo ${service_pid} > ${dump_file}.histo.txt"
			jmap -histo ${service_pid} > ${dump_file}.histo.txt
            ps -ef | grep -v grep | grep "$service_path" |  egrep "$server_port\$" |awk '{print $2}' | xargs kill -9 >/dev/null
            start $service_name call
            exit 0
        fi
        curl -q -s --connect-timeout 2 -m 4 http://127.0.0.1:${server_port}/info  > /dev/null
    done
    echo "运行中..."
}
help(){
	echo "参数: $0"
	echo "	start service_name"
	echo "	stop service_name"
	echo "	restart service_name"
	echo "	status service_name"
	echo "	check_health service_name server_port"
}

$1 $2 $3 $4
