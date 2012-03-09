function vote(id,direction)
{
    $.ajax({url : "/vote/"+direction+"/"+id,
	    success : function (data) {
		$("#error").text("");
		$("#votes").text("Votes: " + data);
	    },
	    error : function (xhr, status, errorThrown) {
		$("#error").text(xhr.responseText);
	    }});
}