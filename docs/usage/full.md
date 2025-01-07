# Decentralized File system (a reference did implementation
## Deployed at \{{registry_server}}) 

* Onboarding a controller. 
    *   Adding a subject where controller is blank 
        /subjects (POST)
        e.g 
```
        curl -H 'content-type:application/json' https://{{registry_server}}/subjects -d 
            '{ "name" : "users/venkatramanm" , 
                "verification_methods" : [{"public_key":"MCowBQYDK2VwAyEA5mMCOi\/N1qwrau6fpMjngWadEEQw\/+OjN3dbDpmzwHI=", 
                "hashing_algorithm" :"Blake512", 
                "purpose": "Authentication" , 
                "type" : "Ed25519"  }] 
            }'

```
*   Adding a verification method.
	/subjects (POST)  to add a subject with one or more verification methods
	/subjects/:subject_id (POST)  to add one or more verification methods
	/subjects/:subject_id/verification_methods (POST) to add one or more verification methods
```
curl -H 'content-type:application/json' https://{{registry_server}}/subjects/users/venkatramanm -d '{ "verification_methods" : [{"public_key":"MCowBQYDK2VuAyEAyTP1GHUfQacEKTo8AUuRFJIKr3Xy5VsMShoCnJiSFAQ=",  "purpose": "KeyAgreement" , "type" : "X25519"  }] }'
{
  "did" : "\/subjects\/users\/venkatramanm"
  ,"verification_methods" : [{
    "did" : "\/subjects\/users\/venkatramanm\/verification_methods\/4e7dc837-5273-4605-a1ca-defe07b9a52c"
    ,"verified" : "N"
  },{
    "did" : "\/subjects\/users\/venkatramanm\/verification_methods\/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5"
    ,"verified" : "N"
  }]
}
```
* List Verification methods 
```
curl -H 'content-type:application/json' https://{{registry_server}}/subjects/users/venkatramanm/verification_methods

[{
  "challenge" : "581671"
  ,"controller" : {
    "did" : "\/subjects\/users\/venkatramanm"
  }
  ,"did" : "\/subjects\/users\/venkatramanm\/verification_methods\/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5"
  ,"hashing_algorithm" : "Blake512"
  ,"public_key" : "MCowBQYDK2VwAyEA5mMCOi\/N1qwrau6fpMjngWadEEQw\/+OjN3dbDpmzwHI="
  ,"purpose" : "Authentication"
  ,"type" : "Ed25519"
  ,"verified" : "N"
},{
  "challenge" : "BF7EvpY\/J\/2jkOih8vGx\/w=="
  ,"controller" : {
    "did" : "\/subjects\/users\/venkatramanm"
  }
  ,"did" : "\/subjects\/users\/venkatramanm\/verification_methods\/4e7dc837-5273-4605-a1ca-defe07b9a52c"
  ,"public_key" : "MCowBQYDK2VuAyEAyTP1GHUfQacEKTo8AUuRFJIKr3Xy5VsMShoCnJiSFAQ="
  ,"purpose" : "KeyAgreement"
  ,"type" : "X25519"
  ,"verified" : "N"
}]
```

*   Verifying the method with a challenge. 
	/subjects/:subject_id/verification_methods/verify/:verification_id POST with payload containing response to a challenge.
		* This could be a signature 
```
curl -H 'content-type:application/json' https://{{registry_server}}/subjects/users/venkatramanm/verification_methods/verify/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5 -d 'ZaaQPRWveCm+vzttPsVx7cZulkGgKqum8+vyihJFwbnnlEWfnw8AFZgT40oHxyarblcoL3XpO1JJG+mTZ+8uBQ==' 


{
  "challenge" : "581671"
  ,"controller" : {
    "did" : "\/subjects\/users\/venkatramanm"
    ,"id" : "5"
  }
  ,"created_at" : "2024-12-19 19:27:26"
  ,"did" : "\/subjects\/users\/venkatramanm\/verification_methods\/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5"
  ,"hashing_algorithm" : "Blake512"
  ,"id" : "15"
  ,"lock_id" : "1"
  ,"name" : "fe2bdd08-753a-45e9-9fc7-b05e52c65fb5"
  ,"public_key" : "MCowBQYDK2VwAyEA5mMCOi\/N1qwrau6fpMjngWadEEQw\/+OjN3dbDpmzwHI="
  ,"purpose" : "Authentication"
  ,"response" : "ZaaQPRWveCm+vzttPsVx7cZulkGgKqum8+vyihJFwbnnlEWfnw8AFZgT40oHxyarblcoL3XpO1JJG+mTZ+8uBQ=="
  ,"type" : "Ed25519"
  ,"updated_at" : "2024-12-19 19:56:26"
  ,"verified" : "Y"
}



Note: the payload is signed(hash("581671")) using the private key corresponding to the verification method

```
		* Decrypted value an encrypted challenge
```
curl -H 'content-type:application/json' https://{{registry_server}}/subjects/users/venkatramanm/verification_methods/verify/4e7dc837-5273-4605-a1ca-defe07b9a52c -d '470331' 


{
  "challenge" : "BF7EvpY\/J\/2jkOih8vGx\/w=="
  ,"controller" : {
    "did" : "\/subjects\/users\/venkatramanm"
    ,"id" : "5"
  }
  ,"created_at" : "2024-12-19 20:03:14"
  ,"did" : "\/subjects\/users\/venkatramanm\/verification_methods\/4e7dc837-5273-4605-a1ca-defe07b9a52c"
  ,"id" : "17"
  ,"lock_id" : "1"
  ,"name" : "4e7dc837-5273-4605-a1ca-defe07b9a52c"
  ,"public_key" : "MCowBQYDK2VuAyEAyTP1GHUfQacEKTo8AUuRFJIKr3Xy5VsMShoCnJiSFAQ="
  ,"purpose" : "KeyAgreement"
  ,"response" : "470331"
  ,"type" : "X25519"
  ,"updated_at" : "2024-12-19 20:14:32"
  ,"verified" : "Y"
}


Note the payload is decrypt(using_create_aes_key(private_key, registry's public key ), "BF7EvpY/J/2jkOih8vGx/w==" ) To solve the challenge 
```
---


```

To get registry's public key: 
------------------------------
 curl -H 'content-type:application/json' https://{{registry_server}}/subjects/{{registry_server}}/verification_methods
[{
  "controller" : {
    "did" : "\/subjects\/{{registry_server}}"
  }
  ,"did" : "\/subjects\/{{registry_server}}\/verification_methods\/{{registry_server}}.k1.X25519"
  ,"public_key" : "MCowBQYDK2VuAyEAYYb0ufC5g8pp9UeMguwutUZoX0PpCl7BtEDTqTERTkQ="
  ,"purpose" : "KeyAgreement"
  ,"type" : "X25519"
  ,"verified" : "Y"
},{
  "controller" : {
    "did" : "\/subjects\/{{registry_server}}"
  }
  ,"did" : "\/subjects\/{{registry_server}}\/verification_methods\/{{registry_server}}.k1.Ed25519"
  ,"hashing_algorithm" : "Blake512"
  ,"public_key" : "MCowBQYDK2VwAyEABkQHXhO\/kEAnxgZiM5bFLlBSY\/Xu2HY58QpWHz5MVto="
  ,"purpose" : "Assertion"
  ,"type" : "Ed25519"
  ,"verified" : "Y"
}]
```


*   Using verified methods to update itself. 
	* Add Documents
```
curl -H 'content-type:application/json' https://{{registry_server}}/subjects/users/venkatramanm/documents -d @x.json 

where x.json contents are 
{
	"name" :"", //Content type is derived based on the name
	"stream" : base64(byte_stream),

}

--- Signing the document manifest.png that was inserted --- 

curl -H 'content-type:application/json' https://{{registry_server}}/subjects/users/venkatramanm/documents/manifest.png -d '{ "signatures" : [{"verification_method" : { "did" : "/subjects/users/venkatramanm/verification_methods/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5"  }, "signature" :"l0XIOXQ0V5oEFUb/53nc3oGkUb4JF0Jz7iRGiTr7HmkS4nn/gTsgtiAkeZWTM4TbZkBqzL6YHeUZzctwTyKxBA=="  }] }

```
	* Add Services.
```

curl -X POST -H 'AUTHORIZATION: Signature keyId="/subjects/users/venkatramanm/verification_methods/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5",algorithm="ed25519",created="1734640856",headers="(request-target) (created) digest",signature="tUCLw+/sjyGx1/NmmPzgA+FiP4C4DMrhKA1rXa/L1u2QQ0uvwcbyh0APi4ZUNp+HU8MtzIgetsomRIEV+CbrDQ=="' -H 'content-type:application/json' --data-binary @payload.json https://{{registry_server}}/subjects/users/venkatramanm 



--payload.json--

{ "services" : [{"end_point":"https://venkatramanm.abc.com/api2", "specification" : { "did" : "/subjects/users/venkatramanm/documents/manifest.png"  } }]}


-- Response --
{
  "did" : "\/subjects\/users\/venkatramanm"
  ,"documents" : [{
    "did" : "\/subjects\/users\/venkatramanm\/documents\/manifest.png"
    ,"signatures" : [{
      "did" : "\/subjects\/users\/venkatramanm\/documents\/manifest.png\/signatures\/f6b29871-54e1-4f29-a9ad-f1ddcb7a0e94"
      ,"verified" : "Y"
    }]
  }]
  ,"services" : [{
    "did" : "\/subjects\/users\/venkatramanm\/services\/5c63de81-e818-4924-8039-5054977f094e"
  }]
  ,"verification_methods" : [{
    "did" : "\/subjects\/users\/venkatramanm\/verification_methods\/4e7dc837-5273-4605-a1ca-defe07b9a52c"
    ,"verified" : "Y"
  },{
    "did" : "\/subjects\/users\/venkatramanm\/verification_methods\/fe2bdd08-753a-45e9-9fc7-b05e52c65fb5"
    ,"verified" : "Y"
  }]
}

```


* Onboarding a subject. 
    *   A Controller can add a subject using its verified method and become the controller for this new subject.
        /subjects (POST)








