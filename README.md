Rollbar Java
=============

This is the notifier library for integrating Java apps with [Rollbar](https://rollbar.com/), the error aggregation service. You will need a Rollbar account: sign up for an account [here](https://rollbar.com/signup/).


Directly
------------------------------

Example:

	RollbarNotifier notifier = new RollbarNotifier(url, apiKey, env);
	
	try {
		doSomethingThatThrowAnException();
	} catch(Throwable throwable) {
		notifier.notify(throwable);
	}

The RollbarNotifier object has several static methods that can be used to notify:
* notify(message)
* notify(message, RollbarAttributeProvider)
* notify(level, message)
* notify(level, message, RollbarAttributeProvider)
* notify(throwable)
* notify(throwable, RollbarAttributeProvider)
* notify(message, throwable)
* notify(message, throwable, RollbarAttributeProvider)
* notify(level, throwable)
* notify(level, throwable, context)
* notify(level, message, throwable, context)


The parameters are:
* Message: String to notify 
* Throwable: Throwable to notify
* RollbarAttributeProvider: An interface, with a default implementation of "RollbarAttributeAdapter".  Methods can be overriden to provide details you wish to be included in the notification.
* Level: Notification level (don't confuse with the Log4j level). By default a throwable notification will be notified with a "error" level and a message notification as a "info" level.

RollbarAttributeProvider
------------------------------

The rollbar notifier use RollbarAttributeProvider to add additional information to the notification and help to solve any detected problem.  To understand the notification message and the possible values see the [rollbar API item] (https://rollbar.com/docs/api_items/).

The best way to use this is to extend "RollbarAttributeProvider" and override methods for information that you wish to provide in the notification.  To understand what possible pieces of information can be provided, checkout the [RollbarAttributeProvider interface] (https://github.com/fullcontact/rollbarNotifier/blob/master/src/main/java/com/muantech/rollbar/java/RollbarAttributeProvider.java).

License
-------

<pre>
This software is licensed under the Apache 2 license, quoted below.

Copyright 2014 Rafael Muñoz Vega

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
</pre>

