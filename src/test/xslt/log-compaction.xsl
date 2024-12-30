<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="./identity-transform.xsl"/>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
  	<xsl:apply-templates select="log"/>
  </xsl:template>
  
  <xsl:template match="log">
    <log>
      <xsl:apply-templates select="*"/>
    </log>
  </xsl:template>
  
  <xsl:template match="record">
  	<record>
    	<xsl:apply-templates select="date"/>
    	<xsl:apply-templates select="message"/>
    </record>
  </xsl:template>

</xsl:transform>
