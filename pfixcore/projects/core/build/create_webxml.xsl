<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                exclude-result-prefixes="cus">

  <xsl:param name="prjname"/>
  <xsl:param name="projectsxmlfile"/>
  <xsl:param name="warmode"/>

  <xsl:variable name="project" select="document(concat('file://', $projectsxmlfile))/projects/project[@name=$prjname]" />
  <xsl:variable name="common" select="document(concat('file://', $projectsxmlfile))/projects/common" />

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes" />

  <xsl:include href="create_lib.xsl"/>

  <xsl:template match="cus:servlet-mapping">
    <xsl:for-each select="$project/servlet">
      <xsl:variable name="active_node">
        <xsl:apply-templates select="./active/node()"/>
      </xsl:variable>
      <xsl:variable name="active">
        <xsl:value-of select="normalize-space($active_node)"/>
      </xsl:variable>

      <xsl:if test="$active = 'true'">
        <servlet-mapping>
          <servlet-name><xsl:value-of select="@name"/></servlet-name>
          <url-pattern>/xml/<xsl:value-of select="@name"/>/*</url-pattern>
        </servlet-mapping>
      </xsl:if>
    </xsl:for-each>
    
    <xsl:if test="not($warmode = 'true')">
      <!-- Default servlet for retrieving static files from /xml/* -->
      <servlet-mapping>
        <servlet-name>static-docroot</servlet-name>
        <url-pattern>/xml/*</url-pattern>
      </servlet-mapping>
    </xsl:if>
    
    <!-- Servlet for static files in / for standalone mode -->
    <servlet-mapping>
      <servlet-name>static-docroot</servlet-name>
      <url-pattern>/</url-pattern>
    </servlet-mapping>

      </xsl:template>

  <xsl:template match="cus:servlet">
    <!-- Docroot servlet -->
    <servlet>
      <servlet-name>static-docroot</servlet-name>
      <servlet-class>de.schlund.pfixxml.DocrootServlet</servlet-class>
      <xsl:if test="$project/defpath/text()">
        <init-param>
          <param-name>defaultpath</param-name>
          <param-value><xsl:value-of select="$project/defpath"/></param-value>
        </init-param>
        <init-param>
          <param-name>passthroughPaths</param-name>
          <param-value>
            <xsl:for-each select="$project/passthrough">
              <xsl:value-of select="text()"/><xsl:text>:</xsl:text>
            </xsl:for-each>
            <xsl:for-each select="$common/apache/passthrough">
              <xsl:value-of select="text()"/><xsl:text>:</xsl:text>
            </xsl:for-each>
          </param-value>
        </init-param>
      </xsl:if>
    </servlet>
    
    <xsl:for-each select="$project/servlet">
      <xsl:variable name="active_node">
        <xsl:apply-templates select="./active/node()"/>
      </xsl:variable>
      <xsl:variable name="active">
        <xsl:value-of select="normalize-space($active_node)"/>
      </xsl:variable>

      <xsl:if test="$active = 'true'">
        <servlet>
          <servlet-name><xsl:value-of select="@name"/></servlet-name>
          <servlet-class><xsl:apply-templates select="class/node()"/></servlet-class>
          <xsl:for-each select="$common/init-param">
            <init-param>
              <param-name><xsl:apply-templates select="./param-name/node()"/></param-name>
              <param-value><xsl:apply-templates select="./param-value/node()"/></param-value>
            </init-param>
          </xsl:for-each>
          <xsl:for-each select="init-param">
            <init-param>
              <param-name><xsl:apply-templates select="./param-name/node()"/></param-name>
              <param-value><xsl:apply-templates select="./param-value/node()"/></param-value>
            </init-param>
          </xsl:for-each>
          <xsl:if test="$common/commonpropfile/node()">
            <init-param>
              <param-name>servlet.commonpropfile</param-name>
              <param-value><xsl:apply-templates select="$common/commonpropfile/node()"/></param-value>
            </init-param>
          </xsl:if>
          <xsl:if test="propfile/node()">
            <init-param>
              <param-name>servlet.propfile</param-name>
              <param-value><xsl:apply-templates select="propfile/node()"/></param-value>
            </init-param>
          </xsl:if>
          <xsl:if test="foreigncontext/node()">
            <init-param>
              <param-name>servlet.contextname</param-name>
              <param-value><xsl:apply-templates select="foreigncontext/node()"/></param-value>
            </init-param>
          </xsl:if>
          <init-param>
          	<param-name>servlet.encoding</param-name>
            <param-value><xsl:apply-templates select="$project/encoding/text()"/></param-value>
          </init-param>
          <init-param>
            <param-name>servlet.dependfile</param-name>
            <param-value><xsl:apply-templates select="$project/depend/node()"/></param-value>
          </init-param>
          <xsl:if test="@autostartup = 'true'">
            <load-on-startup>666</load-on-startup>
          </xsl:if>

        </servlet>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="cus:error-page">
    <xsl:for-each select="$project/tomcat/error-page">
      <error-page>
        <xsl:choose>
          <xsl:when test="exception-type">
            <exception-type><xsl:apply-templates select="exception-type/node()"/></exception-type>
          </xsl:when>
          <xsl:when test="error-code">
            <error-code><xsl:apply-templates select="error-code/node()"/></error-code>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">one element "error-code" or "exception-type" is needed!</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="not(location/node())">
          <xsl:message terminate="yes">one element "location" is needed!</xsl:message>
        </xsl:if>
        <location><xsl:apply-templates select="location/node()"/></location>
      </error-page>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="cus:session-config">
    <!-- if a 'sessiontimeout'-node exists use it, else use default -->
    <xsl:choose>
      <xsl:when test="$project/sessiontimeout">
        <session-config>
          <session-timeout><xsl:value-of select="$project/sessiontimeout"/></session-timeout>
        </session-config>
      </xsl:when>
      <xsl:otherwise>
        <session-config>
          <session-timeout>60</session-timeout>
        </session-config>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="cus:filter">
    <xsl:for-each select="$project/filter">
      
      <xsl:variable name="active_node">
        <xsl:apply-templates select="./active/node()"/>
      </xsl:variable>
      <xsl:variable name="active">
        <xsl:value-of select="normalize-space($active_node)"/>
      </xsl:variable>
      
      <xsl:if test="$active = 'true'">
        <filter>
          <filter-name><xsl:value-of select="@name"/></filter-name>
          <filter-class><xsl:apply-templates select="class/node()"/></filter-class>
          <xsl:if test="foreigncontext/node()">
            <init-param>
              <param-name>contextRef</param-name>
              <param-value><xsl:apply-templates select="foreigncontext/node()"/></param-value>
            </init-param>
          </xsl:if>
          <xsl:apply-templates select="init-param"/>
        </filter>
      </xsl:if>
      
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="cus:filter-mapping">
  
    <xsl:for-each select="$project/servlet/use-filter">
      
      <xsl:variable name="filter-name-node">
        <xsl:apply-templates select="node()"/>
      </xsl:variable>
      <xsl:variable name="filter-name">
        <xsl:value-of select="normalize-space($filter-name-node)"/>
      </xsl:variable>
      
      <xsl:variable name="servlet-name">
        <xsl:value-of select="normalize-space(parent::servlet/@name)"/>
      </xsl:variable>
      
      <xsl:variable name="servlet-active-node">
        <xsl:apply-templates select="parent::servlet/active/node()"/>
      </xsl:variable>
      <xsl:variable name="servlet-active">
        <xsl:value-of select="normalize-space($servlet-active-node)"/>
      </xsl:variable>
      
      <xsl:variable name="filter-active-node">
        <xsl:apply-templates select="$project/filter[@name=$filter-name]/active/node()"/>
      </xsl:variable>
      <xsl:variable name="filter-active">
        <xsl:value-of select="normalize-space($filter-active-node)"/>
      </xsl:variable>
      
      <xsl:if test="$servlet-active = 'true' and $filter-active='true'">
        <filter-mapping>
          <filter-name><xsl:value-of select="$filter-name"/></filter-name>
          <servlet-name><xsl:value-of select="$servlet-name"/></servlet-name>
        </filter-mapping>
      </xsl:if>
      
    </xsl:for-each>
    
    <xsl:apply-templates match="$project/filter-mapping"/>
    
  </xsl:template>

  <xsl:template match="cus:listener">

    <!-- Insert listener for webservices if webservice servlet is configured -->
    <xsl:if test="$project/servlet[@name='webservice' and class/text()='de.schlund.pfixcore.webservice.WebServiceServlet']">
      <listener>
        <listener-class>de.schlund.pfixcore.webservice.WebserviceContextListener</listener-class>
      </listener>
    </xsl:if>
  
    <xsl:for-each select="$project/listener">
    
      <xsl:variable name="active_node">
        <xsl:apply-templates select="./active/node()"/>
      </xsl:variable>
      <xsl:variable name="active">
        <xsl:value-of select="normalize-space($active_node)"/>
      </xsl:variable>
      
      <xsl:if test="$active = 'true'">
        <listener>
          <listener-class><xsl:apply-templates select="class/node()"/></listener-class>
        </listener>
      </xsl:if>
      
    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
