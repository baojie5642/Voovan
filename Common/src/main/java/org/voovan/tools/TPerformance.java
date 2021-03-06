package org.voovan.tools;

import org.voovan.Global;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 系统性能相关
 *
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class TPerformance {

	private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	/**
	 * 内存信息类型枚举
	 */
	public enum MEMTYPE{
		NOHEAP_INIT,
		HEAP_INIT,

		NOHEAP_MAX,
		HEAP_MAX,

		NOHEAP_USAGE,
		HEAP_USAGE,

		NOHEAP_COMMIT,
		HEAP_COMMIT
	}

	/**
	 * 获取当前系统的负载情况
	 * @return 系统的负载情况
	 */
	public static double getSystemLoadAverage(){
		return osmxb.getSystemLoadAverage();
	}

	/**
	 * 获取当前系统 CPU 数
	 * @return 系统 CPU 数
	 */
	public static int getProcessorCount(){
		return osmxb.getAvailableProcessors();
	}

	/**
	 * 获取系统单 CPU 核心的平均负载
	 * @return 单 CPU 核心的平均负载
	 */
	public static double cpuPerCoreLoadAvg(){
		double perCoreLoadAvg = osmxb.getSystemLoadAverage()/osmxb.getAvailableProcessors();
		BigDecimal bg = new BigDecimal(perCoreLoadAvg);
		perCoreLoadAvg = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return perCoreLoadAvg;
	}

	/**
	 * JVM 虚拟机的内存使用情况
	 * @return 内存使用情况
	 * */
	public static double getJVMMemoryUsage(){
		//maxMemory()这个方法返回的是java虚拟机（这个进程）能构从操作系统那里挖到的最大的内存，以字节为单位, -Xmx参数
		//totalMemory()这个方法返回的是java虚拟机现在已经从操作系统那里挖过来的内存大小
		//freeMemory() 当前申请到的内存有多少没有使用的空闲内存
		Runtime runtime = Runtime.getRuntime();
		double memoryUsage = 1-((double)runtime.freeMemory()+(runtime.maxMemory()-runtime.totalMemory()))/(double)runtime.maxMemory();
		BigDecimal bg = new BigDecimal(memoryUsage);
		memoryUsage = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return memoryUsage;
	}

	/**
	 * 获取部分内存信息(栈,非栈)
	 * @param memType 获取的信息类型
	 * @return 当前内存数值
	 */
	public static long getJVMMemoryInfo(MEMTYPE memType){
		if(memType==MEMTYPE.NOHEAP_INIT){
			return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getInit();
		} else if(memType==MEMTYPE.NOHEAP_MAX){
			return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax();
		} else if(memType==MEMTYPE.NOHEAP_USAGE){
			return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
		} else if(memType== MEMTYPE.NOHEAP_COMMIT){
			return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getCommitted();
		} else if(memType==MEMTYPE.HEAP_INIT){
			return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getInit();
		} else if(memType==MEMTYPE.HEAP_MAX){
			return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		} else if(memType==MEMTYPE.HEAP_USAGE){
			return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		} else if(memType==MEMTYPE.HEAP_COMMIT){
			return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted();
		}else{
			throw new RuntimeException("getMemoryInfo function arg error!");
		}
	}

	/**
	 * 获取当前内存信息
	 * @return  内存信息描述对象
	 */
	public static MemoryInfo getJVMMemoryInfo(){
		MemoryInfo memoryInfo = new MemoryInfo();
		memoryInfo.setHeapInit(getJVMMemoryInfo(MEMTYPE.HEAP_INIT));
		memoryInfo.setHeapUsage(getJVMMemoryInfo(MEMTYPE.HEAP_USAGE));
		memoryInfo.setHeapCommit(getJVMMemoryInfo(MEMTYPE.HEAP_COMMIT));
		memoryInfo.setHeapMax(getJVMMemoryInfo(MEMTYPE.HEAP_MAX));

		memoryInfo.setNoHeapInit(getJVMMemoryInfo(MEMTYPE.NOHEAP_INIT));
		memoryInfo.setNoHeapUsage(getJVMMemoryInfo(MEMTYPE.NOHEAP_USAGE));
		memoryInfo.setNoHeapCommit(getJVMMemoryInfo(MEMTYPE.NOHEAP_COMMIT));
		memoryInfo.setNoHeapMax(getJVMMemoryInfo(MEMTYPE.NOHEAP_MAX));
		memoryInfo.setFree(Runtime.getRuntime().freeMemory());
		memoryInfo.setTotal(Runtime.getRuntime().totalMemory());
		memoryInfo.setMax(Runtime.getRuntime().maxMemory());
		return memoryInfo;
	}

	/**
	 * 获取指定进程的 GC 信息
	 * @param pid 进程 Id
	 * @return GC信息
	 * @throws IOException IO 异常
	 */
	public static Map<String, String> getJVMGCInfo(long pid) throws IOException {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		InputStream processInputStream = TEnv.createSysProcess("jstat -gcutil "+pid, null, (File)null).getInputStream();
		String console = new String(TStream.readAll(processInputStream));
		String[] consoleLines = console.split(System.lineSeparator());
		String[] titleLine = consoleLines[0].trim().split("\\s+");
		String[] dataLine = consoleLines[1].trim().split("\\s+");
		for(int i=0; i<titleLine.length; i++){
			result.put(titleLine[i], dataLine[i]);
		}
		return result;
	}

	/**
	 * 获取当前进程的 GC 信息
	 * @return GC信息
	 * @throws IOException IO 异常
	 */
	public static Map<String, String> getJVMGCInfo() throws IOException {
		return getJVMGCInfo(TEnv.getCurrentPID());
	}

	/**
	 * 获取指定进程的对象信息
	 * @param pid		进程 Id
	 * @param regex     对象匹配字符串
	 * @param headCount 返回的对象数
	 * @return 虚拟机中的对象信息
	 * @throws IOException IO 异常
	 */
	public static Map<String,ObjectInfo> getJVMObjectInfo(long pid, String regex, Integer headCount) throws IOException {
		LinkedHashMap<String,ObjectInfo> result = new LinkedHashMap<String,ObjectInfo>();
		InputStream processInputStream = TEnv.createSysProcess("jmap -histo "+pid, null, (File)null).getInputStream();
		String console = new String(TStream.readAll(processInputStream));


		String[] consoleLines = console.split(System.lineSeparator());

		if(headCount<0){
			headCount = consoleLines.length;
		}else{
			headCount=headCount+3;
		}

		for(int lineCount = 3;lineCount<headCount;lineCount++){
			String lineContent = consoleLines[lineCount];
			long count = Long.parseLong(lineContent.substring(5,19).trim());
			long size = Long.parseLong(lineContent.substring(19,34).trim());
			String name = lineContent.substring(34,lineContent.length()).trim();

			if(name.isEmpty()){
				continue;
			}

			if(TString.regexMatch(name,regex) > 0) {
				ObjectInfo objectInfo = new ObjectInfo(name, size, count);
				result.put(name,objectInfo);
			}
		}
		return result;
	}

	/**
	 * 获取当前JVM加载的对象信息(数量,所占内存大小)
	 * @param regex 正则表达式
	 * @param headCount 头部记录数
	 * @return 系统对象信息的Map
	 */
	public static Map<String,TPerformance.ObjectInfo> getJVMObjectInfo(String regex, int headCount) {
		if(regex==null){
			regex = ".*";
		}

		Map<String,TPerformance.ObjectInfo> result;
		try {
			result = TPerformance.getJVMObjectInfo(TEnv.getCurrentPID(), regex, headCount);
		} catch (IOException e) {
			result = new Hashtable<String,TPerformance.ObjectInfo>();
		}

		return result;

	}

	/**
	 * 获取当前 JVM 线程信息描述
	 * @return 线程信息信息集合
	 */
	public static Map<String,Object> getThreadPoolInfo(){
		Map<String,Object> threadPoolInfo = new HashMap<String,Object>();
		ThreadPoolExecutor threadPoolInstance = Global.getThreadPool();
		threadPoolInfo.put("ActiveCount",threadPoolInstance.getActiveCount());
		threadPoolInfo.put("CorePoolSize",threadPoolInstance.getCorePoolSize());
		threadPoolInfo.put("FinishedTaskCount",threadPoolInstance.getCompletedTaskCount());
		threadPoolInfo.put("TaskCount",threadPoolInstance.getTaskCount());
		threadPoolInfo.put("QueueSize",threadPoolInstance.getQueue().size());
		return threadPoolInfo;
	}

	/**
	 * 获取当前 JVM 线程信息描述
	 * @param state 线程状态, nul,返回所有状态的线程
	 * @param withStack 是否包含堆栈信息
	 * @return 线程信息集合
	 */
	public static List<Map<String,Object>> getThreadDetail(String state, boolean withStack){
		ArrayList<Map<String,Object>> threadDetailList = new ArrayList<Map<String,Object>>();
		for(Thread thread : TEnv.getThreads()){
			if(state==null || thread.getState().name().equals(state)) {
				Map<String, Object> threadDetail = new Hashtable<String, Object>();
				threadDetail.put("Name", thread.getName());
				threadDetail.put("Id", thread.getId());
				threadDetail.put("Priority", thread.getPriority());
				threadDetail.put("ThreadGroup", thread.getThreadGroup().getName());
				if (withStack) {
					threadDetail.put("StackTrace", TEnv.getStackElementsMessage(thread.getStackTrace()));
				}

				threadDetail.put("State", thread.getState().name());
				threadDetailList.add(threadDetail);
			}
		}
		return threadDetailList;
	}

	/**
	 * 获取处理器信息
	 * @return 处理器信息 Map
	 */
	public static Map<String,Object>  getProcessorInfo(){
		Map<String,Object> processInfo = new Hashtable<String,Object>();
		processInfo.put("ProcessorCount",TPerformance.getProcessorCount());
		processInfo.put("SystemLoadAverage",TPerformance.getSystemLoadAverage());
		return processInfo;
	}

	/**
	 * 获取JVM信息
	 * @return JVM 信息的 Map
	 */
	public static Map<String,Object> getJVMInfo(){
		Map<String, Object> jvmInfo = new Hashtable<String, Object>();
		for(Map.Entry<Object,Object> entry : System.getProperties().entrySet()){
			jvmInfo.put(entry.getKey().toString(),entry.getValue().toString());
		}
		return jvmInfo;
	}

	/**
	 * 获取当前系统内存使用信息
	 * 		仅限 Linux
	 * @return 数组单位 MB, 1: 内存总大小, 2: 空闲内存大小, 3: 可用内存大小, 4: 交换区大小, 5: 交换区空闲大小
	 * @throws IOException  IO 异常
	 * @throws InterruptedException 中断异常
	 */
	public static Map<String, Integer> getSysMemInfo() throws IOException, InterruptedException
	{
		if(System.getProperty("os.name").toLowerCase().contains("linux")) {
			try(FileInputStream fileInputStream = new FileInputStream("/proc/meminfo")) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
				Map<String, Integer> result = new HashMap<String, Integer>();
				String lineStr = null;
				StringTokenizer token = null;
				while ((lineStr = bufferedReader.readLine()) != null) {
					token = new StringTokenizer(lineStr);
					if (!token.hasMoreTokens())
						continue;

					String tokenStr = token.nextToken();
					if (!token.hasMoreTokens())
						continue;

					if (tokenStr.equalsIgnoreCase("MemTotal:")) {
						result.put("MemTotal", Integer.parseInt(token.nextToken()) / 1024);
					} else if (tokenStr.equalsIgnoreCase("MemFree:")) {
						result.put("MemFree", Integer.parseInt(token.nextToken()) / 1024);
					} else if (tokenStr.equalsIgnoreCase("MemAvailable:")) {
						result.put("MemAvailable", Integer.parseInt(token.nextToken()) / 1024);
					} else if (tokenStr.equalsIgnoreCase("SwapTotal:")) {
						result.put("SwapTotal", Integer.parseInt(token.nextToken()) / 1024);
					} else if (tokenStr.equalsIgnoreCase("SwapFree:")) {
						result.put("SwapFree", Integer.parseInt(token.nextToken()) / 1024);
					}
				}

				return result;
			}
		} else {
			return null;
		}
	}


	/**
	 * 获取当前系统CPU 使用率
	 * 		仅限 Linux
	 * @return cpu信息
	 * @throws IOException  IO 异常
	 * @throws InterruptedException 打断异常
	 */
	public static Map<String, Integer> getSysCpuInfo() throws IOException, InterruptedException {
		if(System.getProperty("os.name").toLowerCase().contains("linux")) {
			Map<String, Integer> result = new HashMap<String, Integer>();
			try(FileInputStream fileInputStream = new FileInputStream("/proc/stat")) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

				StringTokenizer token = new StringTokenizer(bufferedReader.readLine());
				token.nextToken();
				result.put("User", Integer.parseInt(token.nextToken()));
				result.put("Nice", Integer.parseInt(token.nextToken()));
				result.put("Sys", Integer.parseInt(token.nextToken()));
				result.put("Idle", Integer.parseInt(token.nextToken()));

				return result;
			}
		}else{
			return null;
		}
	}

	/**
	 * JVM 中对象信息
	 */
	public static class ObjectInfo{
		private String name;
		private long size;
		private long count;

		public ObjectInfo(String name,long size,long count){
			this.name = name;
			this.size = size;
			this.count = count;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}
	}

	/**
	 * 内存信息对象
	 */
	public static class MemoryInfo{
		private long heapInit;
		private long heapUsage;
		private long heapMax;
		private long heapCommit;
		private long noHeapInit;
		private long noHeapUsage;
		private long noHeapMax;
		private long noHeapCommit;
		private long free;
		private long max;
		private long total;

		public long getFree() {
			return free;
		}

		public void setFree(long free) {
			this.free = free;
		}

		public long getMax() {
			return max;
		}

		public void setMax(long max) {
			this.max = max;
		}

		public long getTotal() {
			return total;
		}

		public void setTotal(long total) {
			this.total = total;
		}

		public long getHeapInit() {
			return heapInit;
		}

		public void setHeapInit(long heapInit) {
			this.heapInit = heapInit;
		}

		public long getHeapUsage() {
			return heapUsage;
		}

		public void setHeapUsage(long heapUsage) {
			this.heapUsage = heapUsage;
		}

		public long getHeapMax() {
			return heapMax;
		}

		public void setHeapMax(long heapMax) {
			this.heapMax = heapMax;
		}

		public long getHeapCommit() {
			return heapCommit;
		}

		public void setHeapCommit(long heapCommit) {
			this.heapCommit = heapCommit;
		}

		public long getNoHeapInit() {
			return noHeapInit;
		}

		public void setNoHeapInit(long noHeapInit) {
			this.noHeapInit = noHeapInit;
		}

		public long getNoHeapUsage() {
			return noHeapUsage;
		}

		public void setNoHeapUsage(long noHeapUsage) {
			this.noHeapUsage = noHeapUsage;
		}

		public long getNoHeapMax() {
			return noHeapMax;
		}

		public void setNoHeapMax(long noHeapMax) {
			this.noHeapMax = noHeapMax;
		}

		public long getNoHeapCommit() {
			return noHeapCommit;
		}

		public void setNoHeapCommit(long noHeapCommit) {
			this.noHeapCommit = noHeapCommit;
		}
	}
}
