<?xml version="1.0" encoding="UTF-8"?>
<MobileElementEntity>
   <description>Password input displayed after selecting Use Password Instead.</description>
   <name>input_password</name>
   <tag></tag>
   <elementGuidId>01d72068-3e67-4a78-9df7-b6ad63006bb1</elementGuidId>
   <selectorMethod>BASIC</selectorMethod>
   <smartLocatorEnabled>false</smartLocatorEnabled>
   <useRalativeImagePath>false</useRalativeImagePath>
   <webElementProperties>
      <isSelected>true</isSelected>
      <matchCondition>equals</matchCondition>
      <name>class</name>
      <type>Main</type>
      <value>android.widget.EditText</value>
      <webElementGuid>389aa49a-f83d-4d8a-926b-664b23581bc5</webElementGuid>
   </webElementProperties>
   <locatorCollection>
      <entry>
         <key>XPATH</key>
         <value>//*[@class = 'android.widget.EditText' and (@password = 'true' or contains(translate(@resource-id, 'PASSWORD', 'password'), 'password'))]</value>
      </entry>
   </locatorCollection>
   <locatorStrategy>XPATH</locatorStrategy>
   <platform>ANDROID</platform>
</MobileElementEntity>
