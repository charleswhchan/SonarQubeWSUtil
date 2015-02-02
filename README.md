[![Build Status](https://travis-ci.org/charleswhchan/SonarQubeWSUtil.svg?branch=master)](https://travis-ci.org/charleswhchan/SonarQubeWSUtil)

# SonarQubeWSUtil 

A small utility to compare the technical debt from all projects within a SonarQube instance.

## Usage

See `--help` for full details.

## Example

Tested against http://nemo.sonarqube.org:
````
project id,project key,metric date1,metric value1,metric date2,metric value2
400155,com.adobe:as3corelib,2012-08-29T03:30:51+0000,14276,2015-01-11T10:04:44+0000,6561
425550,abap-sample-projet,2012-08-28T09:35:59+0000,0,2015-01-18T13:54:57+0000,328
78577,org.apache.activemq:activemq-parent,2010-10-08T21:49:55+0000,1473024,2014-12-01T00:17:49+0000,798803
327690,org.activiti:activiti-root,2011-07-30T15:52:50+0000,241574,2015-01-17T02:49:52+0000,71877
725608,adium-ios,,0,,0
13,net.sf.aislib:aislib,2010-10-08T22:04:00+0000,222528,2015-01-08T14:59:07+0000,36353
766733,com.twitter.ambrose:ambrose,2014-10-18T00:48:15+0000,11739,2015-01-11T13:08:44+0000,4940
690817,org.codehaus.sonar-plugins.visualstudio:sonar-visual-studio-plugin,2014-08-29T19:03:50+0000,150,2015-01-11T16:07:55+0000,175
773515,di.js,2014-10-24T11:26:06+0000,120,2015-01-11T11:17:47+0000,200
445051,angularjs,2012-12-04T10:41:26+0000,27549,2015-01-17T20:44:18+0000,181756
....
````
