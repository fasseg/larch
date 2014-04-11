
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
        data: JSON.stringify(patch),
        dataType: "text",
        contentType: "application/json; charset=utf-8",
        success: function(createdId){
        document.location.href = '/entity/' + createdId;
        }
    });
}

function edit(td, name) {
    var currentValue = $(td).html();
    $(td).unbind('click');
    $(td).removeAttr('onclick');
    $(td).html('<input id="__edit" type="text" name="' + name + '" value="' + currentValue + '"/>');
    $(td).append('<input type="hidden" id="' + name + 'OriginalValue" value="' + currentValue + '"/>');
    $('#__edit').focus();
    $('#__edit').blur(function() {
        stopEdit(td, name);
    });
}

function stopEdit(td, name) {
    var original = $('#' + name + 'OriginalValue').val();
    var newValue = $('#__edit').val();
    $('#__edit').unbind('blur');
    if (original != newValue) {
        $('#updateButton').unbind('click');
        $('#updateButton').removeClass('hidden');
        $('#updateButton').click(function () {
            patchEntity();
        });
        $('#cancelUpdateButton').unbind('click');
        $('#cancelUpdateButton').removeClass('hidden');
        $('#cancelUpdateButton').click(function() {
            document.location.href = document.location.href;
        });
        $(td).addClass('changed');
    }
    $(td).html(newValue);
    $(td).click(function() {
        edit(td, name);
    });
    patch[name] = newValue;
}

function patchEntity() {
    $.ajax({
        url : "/entity/" + $('#entityId').html(),
        type : "PATCH",
        data : JSON.stringify(patch),
        contentType : "application/json",
        success : function() {
            document.location.href = document.location.href;
        },
        error : function(jqXHR, msg, error) {
            alert("Error while patching entity:\n" + msg + "\nServer returned: " + error);
            console.log("Error while patching entity:\n" + msg, error);
        }
    });
}