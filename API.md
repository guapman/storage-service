Base URLs:

* <a href="http://127.0.0.1:8080">http://127.0.0.1:8080</a>

<h1 id="openapi-definition-storage-service">Storage Service</h1>

## uploadFile

<a id="opIduploadFile"></a>

> Code samples

```shell
# You can also use wget
curl -X POST http://127.0.0.1:8080/api/v1/files/upload?userId=string&filename=string&visibility=PRIVATE \
  -H 'Content-Type: application/octet-stream' \
  -H 'Accept: */*'

```

`POST /api/v1/files/upload`

*Upload file to storage as raw binary stream*

> Body parameter

```yaml
string

```

<h3 id="uploadfile-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|userId|query|string|true|none|
|filename|query|string|true|none|
|visibility|query|string|true|none|
|tags|query|array[string]|false|none|
|body|body|string(binary)|false|none|

#### Enumerated Values

|Parameter|Value|
|---|---|
|visibility|PRIVATE|
|visibility|PUBLIC|

> Example responses

> 200 Response

<h3 id="uploadfile-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[FileMetadataDto](#schemafilemetadatadto)|

<aside class="success">
This operation does not require authentication
</aside>

## downloadFile

<a id="opIddownloadFile"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://127.0.0.1:8080/api/v1/files/{fileId}?userId=string \
  -H 'Accept: */*'

```

`GET /api/v1/files/{fileId}`

*Download file from storage*

<h3 id="downloadfile-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|fileId|path|string(uuid)|true|none|
|userId|query|string|true|none|

> Example responses

> 200 Response

<h3 id="downloadfile-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[StreamingResponseBody](#schemastreamingresponsebody)|

<aside class="success">
This operation does not require authentication
</aside>

## deleteFile

<a id="opIddeleteFile"></a>

> Code samples

```shell
# You can also use wget
curl -X DELETE http://127.0.0.1:8080/api/v1/files/{fileId}?userId=string

```

`DELETE /api/v1/files/{fileId}`

*Delete file from storage*

<h3 id="deletefile-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|fileId|path|string(uuid)|true|none|
|userId|query|string|true|none|

<h3 id="deletefile-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|None|

<aside class="success">
This operation does not require authentication
</aside>

## renameFile

<a id="opIdrenameFile"></a>

> Code samples

```shell
# You can also use wget
curl -X PATCH http://127.0.0.1:8080/api/v1/files/{fileId}?userId=string&filename=string \
  -H 'Accept: */*'

```

`PATCH /api/v1/files/{fileId}`

*Rename file*

<h3 id="renamefile-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|fileId|path|string(uuid)|true|none|
|userId|query|string|true|none|
|filename|query|string|true|none|

> Example responses

> 200 Response

<h3 id="renamefile-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[FileMetadataDto](#schemafilemetadatadto)|

<aside class="success">
This operation does not require authentication
</aside>

## listPublicFiles

<a id="opIdlistPublicFiles"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://127.0.0.1:8080/api/v1/files/public?userId=string \
  -H 'Accept: */*'

```

`GET /api/v1/files/public`

*List all public files*

<h3 id="listpublicfiles-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|userId|query|string|true|none|
|tags|query|array[string]|false|none|
|page|query|integer(int32)|false|none|
|size|query|integer(int32)|false|none|
|sortBy|query|string|false|none|
|ascending|query|boolean|false|none|

#### Enumerated Values

|Parameter|Value|
|---|---|
|sortBy|FILENAME|
|sortBy|UPLOAD_DATE|
|sortBy|TAG|
|sortBy|CONTENT_TYPE|
|sortBy|FILE_SIZE|

> Example responses

> 200 Response

<h3 id="listpublicfiles-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[PagedModel](#schemapagedmodel)|

<aside class="success">
This operation does not require authentication
</aside>

## listUserFiles

<a id="opIdlistUserFiles"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://127.0.0.1:8080/api/v1/files/my?userId=string \
  -H 'Accept: */*'

```

`GET /api/v1/files/my`

*List all files uploaded by user*

<h3 id="listuserfiles-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|userId|query|string|true|none|
|tags|query|array[string]|false|none|
|page|query|integer(int32)|false|none|
|size|query|integer(int32)|false|none|
|sortBy|query|string|false|none|
|ascending|query|boolean|false|none|

#### Enumerated Values

|Parameter|Value|
|---|---|
|sortBy|FILENAME|
|sortBy|UPLOAD_DATE|
|sortBy|TAG|
|sortBy|CONTENT_TYPE|
|sortBy|FILE_SIZE|

> Example responses

> 200 Response

<h3 id="listuserfiles-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[PagedModel](#schemapagedmodel)|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_FileMetadataDto">FileMetadataDto</h2>
<!-- backwards compatibility -->
<a id="schemafilemetadatadto"></a>
<a id="schema_FileMetadataDto"></a>
<a id="tocSfilemetadatadto"></a>
<a id="tocsfilemetadatadto"></a>

```json
{
  "id": "497f6eca-6276-4993-bfeb-53cbbbba6f08",
  "filename": "string",
  "tags": [
    "string"
  ],
  "size": 0,
  "visibility": "PRIVATE",
  "contentType": "string",
  "uploadDate": "2019-08-24T14:15:22Z"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string(uuid)|false|none|none|
|filename|string|false|none|none|
|tags|[string]|false|none|none|
|size|integer(int64)|false|none|none|
|visibility|string|false|none|none|
|contentType|string|false|none|none|
|uploadDate|string(date-time)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|visibility|PRIVATE|
|visibility|PUBLIC|

<h2 id="tocS_StreamingResponseBody">StreamingResponseBody</h2>
<!-- backwards compatibility -->
<a id="schemastreamingresponsebody"></a>
<a id="schema_StreamingResponseBody"></a>
<a id="tocSstreamingresponsebody"></a>
<a id="tocsstreamingresponsebody"></a>

```json
{}

```

### Properties

*None*

<h2 id="tocS_PageMetadata">PageMetadata</h2>
<!-- backwards compatibility -->
<a id="schemapagemetadata"></a>
<a id="schema_PageMetadata"></a>
<a id="tocSpagemetadata"></a>
<a id="tocspagemetadata"></a>

```json
{
  "size": 0,
  "number": 0,
  "totalElements": 0,
  "totalPages": 0
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|size|integer(int64)|false|none|none|
|number|integer(int64)|false|none|none|
|totalElements|integer(int64)|false|none|none|
|totalPages|integer(int64)|false|none|none|

<h2 id="tocS_PagedModel">PagedModel</h2>
<!-- backwards compatibility -->
<a id="schemapagedmodel"></a>
<a id="schema_PagedModel"></a>
<a id="tocSpagedmodel"></a>
<a id="tocspagedmodel"></a>

```json
{
  "content": [
    {}
  ],
  "page": {
    "size": 0,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|content|[object]|false|none|none|
|page|[PageMetadata](#schemapagemetadata)|false|none|none|
