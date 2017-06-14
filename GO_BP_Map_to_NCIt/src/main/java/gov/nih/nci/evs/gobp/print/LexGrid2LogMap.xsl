<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
		<xsl:output method="xml" />
		<xsl:template match = "/">
		<Alignment>
		<xsl:apply-templates select="lgRel:associationPredicate" />
		</Alignment>
		
		</xsl:template>
		

		<xsl:template match="lgRel:source">
			<xsl:copy>
				<xsl:copy-of select="@*" />
				<xsl:apply-templates />

			</xsl:copy>



		</xsl:template>
</xsl:stylesheet>