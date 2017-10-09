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
