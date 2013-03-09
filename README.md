Onosendai - A better deck
=========================

A multi-column Twitter client with a focus on list support.

Configuration
-------------

All configuration is stored in `deck.conf` file that lives in the root
of the external storage device, typically `/sdcard/deck.conf`.
If this file does not exist it will created when the UI is launched.

### Example config

```JSON
{
  "accounts": [
    {
      "id": "t0",
      "provider": "twitter",
      "consumerKey": "?ckey?",
      "consumerSecret": "?csecret?",
      "accessToken": "?atoken?",
      "accessSecret": "?asecret?"
    }
  ],
  "columns": [
    {
      "title": "My World",
      "account": "t0",
      "resource": "timeline",
      "refresh": "15min"
    }, {
      "title": "About Me",
      "account": "t0",
      "resource": "mentions",
      "refresh": "15min"
    }, {
      "title": "My Tweets",
      "account": "t0",
      "resource": "me",
      "refresh": "15min"
    }, {
      "title": "My List",
      "account": "t0",
      "resource": "lists/mylist",
      "refresh": "15min"
    }
  ]
}
```

### Background refreshing

Currently all lists will always background refresh on a 15 min time.

