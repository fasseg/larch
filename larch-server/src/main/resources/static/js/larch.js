function createEntity(id, type, label, tags, parentId) {
    var tagList = tags.split(',');
    for (var i = 0; i< tagList.length;i++) {
        tagList[i] = $.trim(tagList[i]);
    }
    var entity = {
        'id' : id,
        'type' : type,
        'label' : label,
        'parentId' : parentId,
        'tags' : tagList
    };
    $.ajax ({
        url: "/entity",
        type: "POST",
        data: JSON.stringify(entity),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function(createdId){
        document.location.href = '/entity/' + createdId;
        }
    });
}

function createBinary(id, name) {
    var binary
}