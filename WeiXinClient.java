   /**
     * 将Map中的数据转换为XML格式的字符串,但是注意转换之后的xml报文只有根节点和一级子节点
     *
     * @param data Map类型数据
     * @return XML格式的字符串
     * @throws Exception
     */
	public static String mapToXml(Map<String, String> data) throws Exception 
	{
		/*建立相关的变量*/
		//建立一个文档工厂的实例
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		//使用文档工厂的实例来建立一个文档创建者
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		//通过这个文档创建者，来创建一个文档，可以看出DOM中的xml是通过Document操作之后转化过来的
		org.w3c.dom.Document document = documentBuilder.newDocument();
		//建立一个节点，这个节点的名字为xml
		org.w3c.dom.Element root = document.createElement("xml");
		//将这个节点直接作为我们创建的文档的子节点，从而作为整个xml报文的根节点
		document.appendChild(root);
		
		/*使用for循环来创建一个xml结构的报文*/
		for(String key : data.keySet()) 
		{
			String value = data.get(key);
			//如果map的值为空就设置这个值为""
			if(value == null) 
			{
				value = "";
			}
			
			value = value.trim();
			//在这个文档中创建一个新的节点
			org.w3c.dom.Element filed = document.createElement(key);
			//为这个节点赋值和命名
			filed.appendChild(document.createTextNode(value));
			//将节点加到根节点之下，做为根节点的子节点
			root.appendChild(filed);
		}
		
		//得到一个转换流工厂
		TransformerFactory tf = TransformerFactory.newInstance();
		//通过转换流工厂来得到一个转换器
		Transformer transformer = tf.newTransformer();
		//使用我们创建的文档，使用DOMSource将其封装为一个输入流
		DOMSource source = new DOMSource(document);
		//设置xml输出数据的相关的属性就是输出xml报文最头上的那一块数据:<?xml version="1.0" encoding="UTF-8" standalone="no"?>
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//建立StringWriter，这个StringWriter就是一个字符流，这个流的作用就是收集输出流，将这些输出流保存到一个buffer总，之后将buffer的数据转化为String
		StringWriter writer = new StringWriter();
		//使用writer的输出流来建立StreamResult，这个StreamResult就是Transformer这个转化类转化的结果的保存者
		StreamResult result = new StreamResult(writer);
		//使用转换器的transform方法，将source中的数据转化为流保存带result中
		transformer.transform(source, result);
		//将writer流中保存的数据的buffer取出来，并且转化为String
		String output = writer.getBuffer().toString(); 
    
		try 
		{
			//关闭流
			writer.close();
		} 
		catch (Exception ex) 
		{
		}
		
		return output;
	}

//-------------------------------------------------------------------------------------------------------------------------------
	/**
	 * XML格式字符串转换为Map，但是注意这个方法只能解析只有一级子节点的xml报文，不能解决有多级子节点的xml报文
	 *
	 * @param strXML
	 *            XML字符串
	 * @return XML数据转换后的Map
	 * @throws Exception
	 */
	public static Map<String, String> xmlToMap(String strXML) throws Exception 
	{
		// 建立保存返回数据的Map
		Map<String, String> data = new HashMap<String, String>();
		// 建立一个文档工厂
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		// 通过文档工厂来建立一个文档创建器
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		// 将返回的xml报文转化为inputStream
		InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
		// 使用创建器documentBuilder的parse方法来将数据流解析为文档
		org.w3c.dom.Document doc = documentBuilder.parse(stream);
		// normalize的作用是将一个文档中的所有的元素全部标准化，主要作用就是去除空的节点，拼接剩余的节点
		doc.getDocumentElement().normalize();
		NodeList nodeList = doc.getDocumentElement().getChildNodes();

		for (int idx = 0; idx < nodeList.getLength(); ++idx) 
		{
			Node node = nodeList.item(idx);
			// 只处理元素节点，其他类型的节点的数据不管
			if(node.getNodeType() == Node.ELEMENT_NODE) 
			{
				org.w3c.dom.Element element = (org.w3c.dom.Element) node;
				data.put(element.getNodeName(), element.getTextContent());
			}
		}

		try 
		{
			stream.close();
		}
		catch (Exception ex) 
		{
		}

		return data;
	}
//-------------------------------------------------------------------------------------------------------------------------------
  /**
    * 这个函数的作用是得到一个32位的唯一的不重复的随机的字符串
    * @return
    */
    public static String generateNonceStr() 
    {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }

//-------------------------------------------------------------------------------------------------------------------------------
/**
 * 通过请求的数据data和密钥key，以及签名类型来得到校验字符串
 * 
 * @param data 请求数据
 * @param key 密钥
 * @param signType 签名类型
 * @return 签名好之后的数据
 * @throws Exception
 */
public static String generateSignature(final Map<String, String> data, String key, SignType signType) throws Exception 
{
	// 得到保存数据的Map的Set，而且这个set中保存的全部都是key
	Set<String> keySet = data.keySet();
	// 将这个set中的数据全部放置到一个字符串数组中
	String[] keyArray = keySet.toArray(new String[keySet.size()]);
	// 使用Array的默认的排序方式（如果要自定义排序方式就重新Array中的compareTo方法），默认排序方式就是ASCII编码先后顺序，可以理解为以首字母先后排序
	Arrays.sort(keyArray);
	StringBuilder sb = new StringBuilder();
		
	for(String k : keyArray) 
	{
		//添加数据
		if(data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
		{
			sb.append(k).append("=").append(data.get(k).trim()).append("&");
		}
	}

	sb.append("key=").append(key);
	
	//开始加密，这一段代码可以自己根据具体需求写
	if(SignType.MD5.equals(signType)) 
	{
		return MD5(sb.toString()).toUpperCase();
	}
	else if(SignType.HMACSHA256.equals(signType)) 
	{
		return HMACSHA256(sb.toString(), key);
	}
	else 
	{
		throw new Exception(String.format("Invalid sign_type: %s", signType));
	}
}

//-------------------------------------------------------------------------------------------------------------------------------
/**
 * 需要证书的请求，这个方法中涉及到了动态加载证书，但是整个函数解释不是很完善，有待完善
 * 而且在使用的时候要自己修改，比如读取证书金额keyStore的加密类型
 * 
 * @param urlSuffix String
 * @param reqData 向wxpay post的请求数据
 * @param connectTimeoutMs 超时时间，单位是毫秒
 * @param readTimeoutMs 超时时间，单位是毫秒
 * @return API返回数据
 * @throws Exception
 */
public String requestWithCert(String strUrl, Map<String, String> reqData, int connectTimeoutMs, int readTimeoutMs) throws Exception 
{
	String UTF8 = "UTF-8";
	//将请求的数据从Map转化为String字符串
	String reqBody = WXPayUtil.mapToXml(reqData);
	//将字符串url转化WieURL对象
	URL httpUrl = new URL(strUrl);
	char[] password = config.getMchID().toCharArray();
	//得到证书数据流
	InputStream certStream = config.getCertStream();
	//keyStore的作用是用来保存密钥和证书，这里使用getInstance来返回指定类型的KeyStore
	KeyStore ks = KeyStore.getInstance("PKCS12");
	//通过一个输入流(证书)来加载(填满)一个KeyStore类，并且添加上password来为这个类来进行加密
	ks.load(certStream, password);
		
	KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	kmf.init(ks, password);

	//返回指定的安全协议，我们使用最新的SSL版本TLS
	SSLContext sslContext = SSLContext.getInstance("TLS");
	sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
	
	//建立HttpsURL的连接
	HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
	
	//向远程服务输出数据
	httpURLConnection.setDoOutput(true);
	httpURLConnection.setRequestMethod("POST");
	httpURLConnection.setConnectTimeout(connectTimeoutMs);
	httpURLConnection.setReadTimeout(readTimeoutMs);
	httpURLConnection.connect();
	OutputStream outputStream = httpURLConnection.getOutputStream();
	outputStream.write(reqBody.getBytes(UTF8));
	//得到远程服务器返回数据
	InputStream inputStream = httpURLConnection.getInputStream();
	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, UTF8));
	final StringBuffer stringBuffer = new StringBuffer();
	String line = null;
	while((line = bufferedReader.readLine()) != null) 
	{
		stringBuffer.append(line);
	}
		
	String resp = stringBuffer.toString();
	if(stringBuffer != null) 
	{
		try 
		{
			bufferedReader.close();
		} 
		catch (IOException e) 
		{
			// e.printStackTrace();
		}
	}
		
	if(inputStream != null) 
	{
		try 
		{
			inputStream.close();
		} 
		catch (IOException e) 
		{
			// e.printStackTrace();
		}
	}
		
	if(outputStream != null) 
	{
		try 
		{
			outputStream.close();
		} 
		catch (IOException e) 
		{
			// e.printStackTrace();
		}
	}
		
	if (certStream != null) 
	{
		try 
		{
			certStream.close();
		} 
		catch (IOException e) 
		{
		}
	}
	
	return resp;
}
