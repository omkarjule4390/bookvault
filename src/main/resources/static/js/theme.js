(function(){
  var root=document.documentElement,btn=document.getElementById('themeToggle');
  function apply(t){root.setAttribute('data-theme',t);if(btn){var d=t==='dark';btn.setAttribute('aria-label',d?'Switch to light mode':'Switch to dark mode');btn.innerHTML='<i class="fa-solid '+(d?'fa-sun':'fa-moon')+'"></i>';}}
  apply(root.getAttribute('data-theme')||'light');
  if(btn)btn.addEventListener('click',function(){var t=root.getAttribute('data-theme')==='dark'?'light':'dark';try{localStorage.setItem('bv_theme',t)}catch(e){}apply(t)});
  var nav=document.getElementById('navLinks'),mt=document.getElementById('menuToggle');
  if(mt)mt.addEventListener('click',function(){var o=nav.classList.toggle('open');mt.setAttribute('aria-expanded',o)});
  if(nav)nav.addEventListener('click',function(e){if(e.target.closest('.nav-btn'))nav.classList.remove('open')});
  // label table cells so rows become cards on phones
  function label(tbl){var h=[].map.call(tbl.querySelectorAll('thead th'),function(x){return x.textContent.trim()});
    tbl.querySelectorAll('tbody tr').forEach(function(r){[].forEach.call(r.children,function(td,i){if(h[i]&&td.getAttribute('data-label')!==h[i])td.setAttribute('data-label',h[i])})})}
  document.querySelectorAll('.data-table').forEach(function(t){var b=t.querySelector('tbody');
    new MutationObserver(function(){label(t)}).observe(b,{childList:true});label(t)});
})();
