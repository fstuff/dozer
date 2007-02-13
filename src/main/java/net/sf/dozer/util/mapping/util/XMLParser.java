/*
 * Copyright 2005-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.dozer.util.mapping.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.dozer.util.mapping.converters.CustomConverterContainer;
import net.sf.dozer.util.mapping.converters.CustomConverterDescription;
import net.sf.dozer.util.mapping.fieldmap.AllowedExceptionContainer;
import net.sf.dozer.util.mapping.fieldmap.ClassMap;
import net.sf.dozer.util.mapping.fieldmap.Configuration;
import net.sf.dozer.util.mapping.fieldmap.CopyByReference;
import net.sf.dozer.util.mapping.fieldmap.CopyByReferenceContainer;
import net.sf.dozer.util.mapping.fieldmap.DozerClass;
import net.sf.dozer.util.mapping.fieldmap.ExcludeFieldMap;
import net.sf.dozer.util.mapping.fieldmap.Field;
import net.sf.dozer.util.mapping.fieldmap.FieldMap;
import net.sf.dozer.util.mapping.fieldmap.GenericFieldMap;
import net.sf.dozer.util.mapping.fieldmap.Hint;
import net.sf.dozer.util.mapping.fieldmap.Mappings;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author garsombke.franz
 */
public class XMLParser extends MapperConstants {

  private static final Log log = LogFactory.getLog(XMLParser.class);

  private final Mappings mappings = new Mappings();

  public Mappings parse(InputStream inputSource) throws SAXException, ParserConfigurationException, IOException,
      ClassNotFoundException {
    DocumentBuilderFactory factory = createDocumentBuilderFactory();
    DocumentBuilder builder = createDocumentBuilder(factory);
    Document document = builder.parse(inputSource);
    Element theRoot = document.getDocumentElement();
    NodeList nl = theRoot.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element ele = (Element) node;
        log.info("name: " + ele.getNodeName());
        if (CONFIGURATION_ELEMENT.equals(ele.getNodeName())) {
          parseConfiguration(ele);
        } else if (MAPPING_ELEMENT.equals(ele.getNodeName())) {
          parseMapping(ele);
        }
      }
    }
    return mappings;
  }

  private void parseMapping(Element ele) throws ClassNotFoundException {
    ClassMap classMap = new ClassMap();
    mappings.getMapping().add(classMap);
    if (StringUtils.isNotEmpty(ele.getAttribute(DATE_FORMAT_ATTRIBUTE))) {
      classMap.setDateFormat(ele.getAttribute(DATE_FORMAT_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(MAP_NULL_ATTRIBUTE))) {
      classMap.setMapNull(BooleanUtils.toBoolean(ele.getAttribute(MAP_NULL_ATTRIBUTE)));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(MAP_EMPTY_STRING_ATTRIBUTE))) {
      classMap.setMapEmptyString(BooleanUtils.toBoolean(ele.getAttribute(MAP_EMPTY_STRING_ATTRIBUTE)));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(BEAN_FACTORY_ATTRIBUTE))) {
      classMap.setBeanFactory(ele.getAttribute(BEAN_FACTORY_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(WILDCARD_ATTRIBUTE))) {
      classMap.setWildcard(BooleanUtils.toBoolean(ele.getAttribute(WILDCARD_ATTRIBUTE)));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(STOP_ON_ERRORS_ATTRIBUTE))) {
      classMap.setStopOnErrors(BooleanUtils.toBoolean(ele.getAttribute(STOP_ON_ERRORS_ATTRIBUTE)));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(MAPID_ATTRIBUTE))) {
      classMap.setMapId(ele.getAttribute(MAPID_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(TYPE_ATTRIBUTE))) {
      classMap.setType(ele.getAttribute(TYPE_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(IS_ACCESSIBLE_ATTRIBUTE))) {
      classMap.setAccessible(BooleanUtils.toBoolean(ele.getAttribute(IS_ACCESSIBLE_ATTRIBUTE)));
    }
    NodeList nl = ele.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        log.info("config name: " + element.getNodeName());
        log.info("  value: " + element.getFirstChild().getNodeValue());
        if (CLASS_A_ELEMENT.equals(element.getNodeName())) {
          DozerClass source = new DozerClass();
          source.setName(element.getFirstChild().getNodeValue().trim());
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_GET_METHOD_ATTRIBUTE))) {
            source.setMapGetMethod(element.getAttribute(MAP_GET_METHOD_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_SET_METHOD_ATTRIBUTE))) {
            source.setMapSetMethod(element.getAttribute(MAP_SET_METHOD_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(BEAN_FACTORY_ATTRIBUTE))) {
            source.setBeanFactory(element.getAttribute(BEAN_FACTORY_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(FACTORY_BEANID_ATTRIBUTE))) {
            source.setFactoryBeanId(element.getAttribute(FACTORY_BEANID_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(CREATE_METHOD_ATTRIBUTE))) {
            source.setCreateMethod(element.getAttribute(CREATE_METHOD_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_NULL_ATTRIBUTE))) {
            source.setMapNull(Boolean.valueOf(element.getAttribute(MAP_NULL_ATTRIBUTE)));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_EMPTY_STRING_ATTRIBUTE))) {
            source.setMapEmptyString(Boolean.valueOf(element.getAttribute(MAP_EMPTY_STRING_ATTRIBUTE)));
          }
          classMap.setSourceClass(source);
        }
        if (CLASS_B_ELEMENT.equals(element.getNodeName())) {
          DozerClass dest = new DozerClass();
          dest.setName(element.getFirstChild().getNodeValue().trim());
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_GET_METHOD_ATTRIBUTE))) {
            dest.setMapGetMethod(element.getAttribute(MAP_GET_METHOD_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_SET_METHOD_ATTRIBUTE))) {
            dest.setMapSetMethod(element.getAttribute(MAP_SET_METHOD_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(BEAN_FACTORY_ATTRIBUTE))) {
            dest.setBeanFactory(element.getAttribute(BEAN_FACTORY_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(FACTORY_BEANID_ATTRIBUTE))) {
            dest.setFactoryBeanId(element.getAttribute(FACTORY_BEANID_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(CREATE_METHOD_ATTRIBUTE))) {
            dest.setCreateMethod(element.getAttribute(CREATE_METHOD_ATTRIBUTE));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_NULL_ATTRIBUTE))) {
            dest.setMapNull(Boolean.valueOf(element.getAttribute(MAP_NULL_ATTRIBUTE)));
          }
          if (StringUtils.isNotEmpty(element.getAttribute(MAP_EMPTY_STRING_ATTRIBUTE))) {
            dest.setMapEmptyString(Boolean.valueOf(element.getAttribute(MAP_EMPTY_STRING_ATTRIBUTE)));
          }
          classMap.setDestClass(dest);
        }
        if (FIELD_ELEMENT.equals(element.getNodeName())) {
          parseGenericFieldMap(element, classMap);
        } else if (FIELD_EXCLUDE_ELEMENT.equals(element.getNodeName())) {
          parseFieldExcludeMap(element, classMap);
        }
      }
    }
  }

  private void parseFieldExcludeMap(Element ele, ClassMap classMap) {
    ExcludeFieldMap efm = new ExcludeFieldMap();
    if (StringUtils.isNotEmpty(ele.getAttribute(TYPE_ATTRIBUTE))) {
      efm.setType(ele.getAttribute(TYPE_ATTRIBUTE));
    }
    classMap.addFieldMapping(efm);
    NodeList nl = ele.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        log.info("config name: " + element.getNodeName());
        log.info("  value: " + element.getFirstChild().getNodeValue());
        parseFieldElements(element, efm);
      }
    }
  }

  private void parseFieldElements(Element element, FieldMap fieldMap) {
    if (A_ELEMENT.equals(element.getNodeName())) {
      fieldMap.setSourceField(parseField(element));
    }
    if (B_ELEMENT.equals(element.getNodeName())) {
      fieldMap.setDestField(parseField(element));
    }
  }

  private void parseGenericFieldMap(Element ele, ClassMap classMap) {
    GenericFieldMap gfm = new GenericFieldMap();
    classMap.addFieldMapping(gfm);
    if (StringUtils.isNotEmpty(ele.getAttribute(COPY_BY_REFERENCE_ATTRIBUTE))) {
      gfm.setCopyByReference(BooleanUtils.toBoolean(ele.getAttribute(COPY_BY_REFERENCE_ATTRIBUTE)));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(MAPID_ATTRIBUTE))) {
      gfm.setMapId(ele.getAttribute(MAPID_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(TYPE_ATTRIBUTE))) {
      gfm.setType(ele.getAttribute(TYPE_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(CUSTOM_CONVERTER_ATTRIBUTE))) {
      gfm.setCustomConverter(ele.getAttribute(CUSTOM_CONVERTER_ATTRIBUTE));
    }
    
    
    parseFieldMap(ele, gfm);
  }

  private void parseFieldMap(Element ele, GenericFieldMap fieldMap) {
    if (StringUtils.isNotEmpty(ele.getAttribute(RELATIONSHIP_TYPE_ATTRIBUTE))) {
      fieldMap.setRelationshipType(ele.getAttribute(RELATIONSHIP_TYPE_ATTRIBUTE));
    }
    NodeList nl = ele.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        log.info("config name: " + element.getNodeName());
        log.info("  value: " + element.getFirstChild().getNodeValue());
        parseFieldElements(element, fieldMap);
        if (SOURCE_TYPE_HINT_ELEMENT.equals(element.getNodeName())) {
          Hint sourceHint = new Hint();
          sourceHint.setHintName(element.getFirstChild().getNodeValue().trim());
          fieldMap.setSourceTypeHint(sourceHint);
        }
        if (DESTINATION_TYPE_HINT_ELEMENT.equals(element.getNodeName())) {
          Hint destHint = new Hint();
          destHint.setHintName(element.getFirstChild().getNodeValue().trim());
          fieldMap.setDestinationTypeHint(destHint);
        }
      }
    }
  }

  private boolean isIndexed(String fieldName) {
    return (fieldName != null) && (fieldName.matches(".+\\[\\d+\\]"));
  }

  private String getFieldNameOfIndexedField(String fieldName) {
    return fieldName == null ? null : fieldName.replaceAll("\\[\\d+\\]", "");
  }

  private int getIndexOfIndexedField(String fieldName) {
    return Integer.parseInt(fieldName.replaceAll(".*\\[", "").replaceAll("\\]", ""));
  }

  private Field parseField(Element ele) {
    Field rvalue = null;
    String type = null;
    String fieldName;
    String name = (ele.getFirstChild().getNodeValue().trim());
    if (isIndexed(name)) {
      fieldName = getFieldNameOfIndexedField(name);
    } else {
      fieldName = name;
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(TYPE_ATTRIBUTE))) {
      type = ele.getAttribute(TYPE_ATTRIBUTE);
    }
    rvalue = new Field(fieldName, type);
    if (isIndexed(name)) {
      rvalue.setIndexed(true);
      rvalue.setIndex(getIndexOfIndexedField(name));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(DATE_FORMAT_ATTRIBUTE))) {
      rvalue.setDateFormat(ele.getAttribute(DATE_FORMAT_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(THE_GET_METHOD_ATTRIBUTE))) {
      rvalue.setTheGetMethod(ele.getAttribute(THE_GET_METHOD_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(THE_SET_METHOD_ATTRIBUTE))) {
      rvalue.setTheSetMethod(ele.getAttribute(THE_SET_METHOD_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(MAP_GET_METHOD_ATTRIBUTE))) {
      rvalue.setMapGetMethod(ele.getAttribute(MAP_GET_METHOD_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(MAP_SET_METHOD_ATTRIBUTE))) {
      rvalue.setMapSetMethod(ele.getAttribute(MAP_SET_METHOD_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(KEY_ATTRIBUTE))) {
      rvalue.setKey(ele.getAttribute(KEY_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(CREATE_METHOD_ATTRIBUTE))) {
      rvalue.setCreateMethod(ele.getAttribute(CREATE_METHOD_ATTRIBUTE));
    }
    if (StringUtils.isNotEmpty(ele.getAttribute(IS_ACCESSIBLE_ATTRIBUTE))) {
      rvalue.setAccessible(BooleanUtils.toBoolean(ele.getAttribute(IS_ACCESSIBLE_ATTRIBUTE)));
    }
    return rvalue;
  }

  private void parseConfiguration(Element ele) throws ClassNotFoundException {
    Configuration config = new Configuration();
    mappings.setConfiguration(config);
    NodeList nl = ele.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        log.info("config name: " + element.getNodeName());
        log.info("  value: " + element.getFirstChild().getNodeValue());
        if (STOP_ON_ERRORS_ELEMENT.equals(element.getNodeName())) {
          config.setStopOnErrors(BooleanUtils.toBoolean(element.getFirstChild().getNodeValue().trim()));
        } else if (DATE_FORMAT_ELEMENT.equals(element.getNodeName())) {
          config.setDateFormat(element.getFirstChild().getNodeValue().trim());
        } else if (WILDCARD_ELEMENT.equals(element.getNodeName())) {
          config.setWildcard(BooleanUtils.toBoolean(element.getFirstChild().getNodeValue().trim()));
        } else if (BEAN_FACTORY_ELEMENT.equals(element.getNodeName())) {
          config.setBeanFactory(element.getFirstChild().getNodeValue().trim());
        } else if (IS_ACCESSIBLE_ELEMENT.equals(element.getNodeName())) {
          config.setAccessible(BooleanUtils.toBoolean(element.getFirstChild().getNodeValue().trim()));
        } else if (CUSTOM_CONVERTERS_ELEMENT.equals(element.getNodeName())) {
          parseCustomConverters(element, config);
        } else if (COPY_BY_REFERENCES_ELEMENT.equals(element.getNodeName())) {
          parseCopyByReferences(element, config);
        } else if (ALLOWED_EXCEPTIONS_ELEMENT.equals(element.getNodeName())) {
          parseAllowedExceptions(element, config);
        }
      }
    }
  }

  private void parseCustomConverters(Element ele, Configuration config) throws ClassNotFoundException {
    CustomConverterContainer container = new CustomConverterContainer();
    config.setCustomConverters(container);
    NodeList nl = ele.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        log.info("config name: " + element.getNodeName());
        log.info("  value: " + element.getFirstChild().getNodeValue());
        if (CONVERTER_ELEMENT.equals(element.getNodeName())) {
          CustomConverterDescription customConverter = new CustomConverterDescription();
          container.addConverter(customConverter);
          customConverter.setType(Thread.currentThread().getContextClassLoader().loadClass(element.getAttribute(TYPE_ATTRIBUTE)));
          NodeList list = element.getChildNodes();
          for (int x = 0; x < list.getLength(); x++) {
            Node node1 = list.item(x);
            if (node1 instanceof Element) {
              Element element1 = (Element) node1;
              if (CLASS_A_ELEMENT.equals(element1.getNodeName())) {
                customConverter.setClassA(Thread.currentThread().getContextClassLoader().loadClass(element1.getFirstChild().getNodeValue().trim()));
              } else if (CLASS_B_ELEMENT.equals(element1.getNodeName())) {
                customConverter.setClassB(Thread.currentThread().getContextClassLoader().loadClass(element1.getFirstChild().getNodeValue().trim()));
              }
            }
          }
        }
      }
    }
  }

  private void parseCopyByReferences(Element ele, Configuration config) {
    CopyByReferenceContainer container = new CopyByReferenceContainer();
    config.setCopyByReferences(container);
    NodeList nl = ele.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        log.info("config name: " + element.getNodeName());
        log.info("  value: " + element.getFirstChild().getNodeValue());
        if (COPY_BY_REFERENCE_ELEMENT.equals(element.getNodeName())) {
          CopyByReference cbr = new CopyByReference();
          container.getCopyByReferences().add(cbr);
          cbr.setReferenceName(element.getFirstChild().getNodeValue().trim());
        }
      }
    }
  }
  private void parseAllowedExceptions(Element ele, Configuration config) {
	    AllowedExceptionContainer container = new AllowedExceptionContainer();
	    config.setAllowedExceptions(container);
	    NodeList nl = ele.getChildNodes();
	    for (int i = 0; i < nl.getLength(); i++) {
	      Node node = nl.item(i);
	      if (node instanceof Element) {
	        Element element = (Element) node;
	        log.info("config name: " + element.getNodeName());
	        log.info("  value: " + element.getFirstChild().getNodeValue());
	        if (ALLOWED_EXCEPTION_ELEMENT.equals(element.getNodeName())) {
	        	try {
	        		Class ex = Class.forName(element.getFirstChild().getNodeValue());
	        		if (!RuntimeException.class.isAssignableFrom(ex)) {
	        			throw new ClassNotFoundException();
	        		}
	        		container.getExceptions().add(ex);
	        	} catch (ClassNotFoundException e) {
	        		log.error("Class not found or does not extend RuntimeException: " + element.getFirstChild().getNodeValue());
	        	}
	        }
	      }
	    }
	  }

  /**
   * Create a JAXP DocumentBuilderFactory that this bean definition reader will use for parsing XML documents. Can be
   * overridden in subclasses, adding further initialization of the factory.
   * 
   * @return the JAXP DocumentBuilderFactory
   * @throws ParserConfigurationException
   *           if thrown by JAXP methods
   */
  protected DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    factory.setNamespaceAware(false);
    factory.setIgnoringElementContentWhitespace(true);
    return factory;
  }

  /**
   * Create a JAXP DocumentBuilder that this bean definition reader will use for parsing XML documents. Can be
   * overridden in subclasses, adding further initialization of the builder.
   * 
   * @param factory
   *          the JAXP DocumentBuilderFactory that the DocumentBuilder should be created with
   * @return the JAXP DocumentBuilder
   * @throws ParserConfigurationException
   *           if thrown by JAXP methods
   */
  protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory) throws ParserConfigurationException {

    DocumentBuilder docBuilder = factory.newDocumentBuilder();
    docBuilder.setErrorHandler(new DozerDefaultHandler());
    docBuilder.setEntityResolver(new DozerResolver());
    return docBuilder;
  }

  class DozerDefaultHandler extends DefaultHandler {
	private final Log log = LogFactory.getLog(DozerDefaultHandler.class);

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      log.info("tag: " + qName);
    }

    public void warning(SAXParseException e) throws SAXException {
      // you can choose not to handle it
      throw new SAXException(getMessage("Warning", e));
    }

    public void error(SAXParseException e) throws SAXException {
      throw new SAXException(getMessage("Error", e));
    }

    public void fatalError(SAXParseException e) throws SAXException {
      throw new SAXException(getMessage("Fatal Error", e));
    }

    private String getMessage(String level, SAXParseException e) {
      return ("Parsing " + level + "\n" + "Line:    " + e.getLineNumber() + "\n" + "URI:     " + e.getSystemId() + "\n"
          + "Message: " + e.getMessage());
    }
  }
}