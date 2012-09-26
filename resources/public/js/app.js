$(function() {
    $(document).pjax('a[data-pjax]')

    stats = function() {
        $('#why').on('submit', function(e) {
            e.preventDefault()
            var text = $('#why').find('textarea').val();
            $.post('/thoughts', {'text': text}, function() {
                $('#why').remove();
            }, 'json')
        })
    }
    
    stats()

    var overlay;
    $('img').on('mouseenter', (function(e) {
        var img = $(e.target);
        var pos = img.offset();
        var div = document.createElement("div")
        $div = $(div)
        $div.css({
            'position': 'absolute',
            'top': pos.top,
            'left': pos.left,
            'z-index': '1000',
            'width': img.width(),
            'height': img.height()
        }).addClass('overlay')        
        $div.append("<h1>Choose</h1>")
        overlay = $div
        $('body').append(overlay)
        overlay.on('mouseleave', function() { overlay.remove() })
        overlay.on('click', function() {
            var choice = img.attr('data-choice');
            var payload = { 'choice': choice}
            $.post('/choose', payload, function(response) {
                $.pjax({
                    url: '/stats',
                    container: '#container'
                })
            }, 'json')
        })
    }))
})
