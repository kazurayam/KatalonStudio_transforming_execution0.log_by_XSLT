<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="./identity-transform.xsl"/>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
  	<xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="log">
    <log>
      <xsl:apply-templates select="record[contains(level,'FAILED')]"/>
    </log>
  </xsl:template>

  <xsl:template match="record">
  	<record>
    	<xsl:apply-templates select="date"/>
      <xsl:apply-templates select="level"/>
    	<xsl:apply-templates select="message"/>
    </record>
  </xsl:template>

  <!--
  we will convert
    <date>2024-12-30T14:20:35.937395Z</date>
  to
    <date>2024-12-30 14:20:35</date>
  -->
  <xsl:template match="date">
    <date><xsl:value-of select="concat(substring-before(.,'T'),
                                ' ',
                                substring(substring-after(.,'T'), 1, 8))" /></date>
  </xsl:template>

</xsl:transform>
