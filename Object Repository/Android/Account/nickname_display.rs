<?xml version="1.0" encoding="UTF-8"?>
<MobileElementEntity>
   <description></description>
   <name>nickname_display</name>
   <tag></tag>
   <elementGuidId>c5c41a3e-7d96-4e87-bd6a-beeb83ce6146</elementGuidId>
   <selectorMethod>BASIC</selectorMethod>
   <smartLocatorEnabled>false</smartLocatorEnabled>
   <useRalativeImagePath>false</useRalativeImagePath>
   <locatorCollection>
      <entry>
         <key>ANDROID_UI_AUTOMATOR</key>
         <value>new UiSelector()</value>
      </entry>
      <entry>
         <key>XPATH</key>
         <value>//*[contains(@content-desc, '${nickname}') or contains(@text, '${nickname}')]</value>
      </entry>
   </locatorCollection>
   <locatorStrategy>XPATH</locatorStrategy>
   <platform>ANDROID</platform>
</MobileElementEntity>
