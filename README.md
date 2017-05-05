## ===========WAP push PDU resolve for android============
About WAP(Wirless Application Protocol), please visit this website: [http://www.wapforum.org](http://www.wapforum.org).

WAP PDU is broadcasted by android framework, APP with receiver can receive it and resolve PDU data to notify user.
To realize this purpose, a receiver with the permission `android.permission.BROADCAST_WAP_PUSH` and intent-filter with action `android.provider.Telephony.WAP_PUSH_DELIVER` should be declared and then decode the WAP PDU using the provided utility class.
