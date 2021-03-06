package org.voovan.network;

/**
 * 事件处理线程
 * 
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class EventThread  implements Runnable{
	
	private Event event;
	
	/**
	 * 事件处理 Thread
	 * @param event 事件
	 */
	public EventThread(Event event){
		this.event = event;
	}
	
	/**
	 * 获取事件
	 * @return 事件对象
	 */
	public Event getEvent(){
		return event;
	}

	@Override
	public void run() { 
		EventProcess.process(event);
	}
	
}
