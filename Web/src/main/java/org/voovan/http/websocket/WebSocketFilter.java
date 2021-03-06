package org.voovan.http.websocket;

/**
 * 过滤器接口
 * 
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public interface WebSocketFilter {
	/**
	 * 过滤器解密函数,接收事件(onRecive)前调用
	 * 			onRecive事件前调用
	 * @param session  session 对象
	 * @param object   解码对象,上一个过滤器的返回值
	 * @return 解码后对象
	 */
	public Object decode(WebSocketSession session, Object object);

	/**
	 * 过滤器加密函数,发送事件(onSend)前调用
	 * 			send事件前调用
	 * @param session 	session 对象
	 * @param object    编码对象,上一个过滤器的返回值
	 * @return 编码后对象
	 * 			最后一个过滤器返回的数据只支持三种数据类型: ByteBuffer, String, byte[]
	 */
	public Object encode(WebSocketSession session, Object object);
	
}
