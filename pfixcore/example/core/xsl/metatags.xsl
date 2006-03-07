<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <xsl:import href="core/xsl/default_copy.xsl"/>
  <xsl:import href="core/xsl/include.xsl"/>
  <xsl:import href="core/xsl/utils.xsl"/>

  <cus:custom_xsl/>

  <xsl:param name="page"/>
  <xsl:param name="navigation"><cus:navigation/></xsl:param>
  <xsl:param name="navitree" select="document($navigation)/make/navigation"/>
  
  <xsl:param name="lang"><cus:lang/></xsl:param>
  <xsl:variable name="product"><cus:product/></xsl:variable>
  
</xsl:stylesheet>
