$(function(argument) {
  $('[type="checkbox"]').bootstrapSwitch(
    {size: 'mini'}
  );
});

$('[name="buildAll"]').on('switchChange.bootstrapSwitch', function(event, state) {
  $('.build').prop('checked', state);
  $('.build').bootstrapSwitch('state', state);
});

function changeBackgroundColor(color) {
  document.body.style.background = color;
}