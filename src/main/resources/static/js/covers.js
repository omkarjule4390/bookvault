/* Designed book covers (SVG, 2:3) + home search helpers. Covers are generated from each book's category and title; no data is invented. */
(function(){
var T={
 tech:{k:/tech|software|comput|program|engineer|data|code|web|system/i,c:['#23395B','#77D3FC','#3C8D93'],m:'circuit'},
 selfhelp:{k:/self|help|habit|mind|personal|wellness|motivat/i,c:['#2F7D5B','#9BD3A8','#1E4D3A'],m:'leaf'},
 fantasy:{k:/fantasy|magic|myth|dragon/i,c:['#A99BE8','#4B3F8F','#F7C6E0'],m:'mountain'},
 science:{k:/scien|physic|space|chem|bio|astro|math/i,c:['#1B2A49','#A99BE8','#77D3FC'],m:'planet'},
 business:{k:/business|manage|econom|financ|market|leader/i,c:['#EDEAE2','#23395B','#E6A23C'],m:'bars'},
 education:{k:/educat|learn|academ|study|school|textbook/i,c:['#3C8D93','#F7F5F0','#E6A23C'],m:'book'},
 history:{k:/histor|architect|ancient|civil|war|culture/i,c:['#E6A23C','#8A5A10','#F3D9A4'],m:'columns'},
 fiction:{k:/fiction|novel|story|literature|general/i,c:['#F07867','#E6A23C','#23395B'],m:'sunset'}
};
var KEYS=Object.keys(T);
function hash(s){var h=7;for(var i=0;i<s.length;i++)h=(h*31+s.charCodeAt(i))>>>0;return h}
function esc(s){return String(s==null?'':s).replace(/[&<>"']/g,function(c){return{'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]})}
function key(b){var t=(b.category||'')+' '+(b.title||'');for(var i=0;i<KEYS.length;i++)if(T[KEYS[i]].k.test(b.category||''))return KEYS[i];
  for(i=0;i<KEYS.length;i++)if(T[KEYS[i]].k.test(t))return KEYS[i];return KEYS[hash(t)%KEYS.length]}
function lines(t){var w=String(t||'').split(/\s+/),o=[],l='';w.forEach(function(x){if((l+' '+x).trim().length>14&&l){o.push(l);l=x}else l=(l+' '+x).trim()});if(l)o.push(l);
  if(o.length>3){o=o.slice(0,3);o[2]=o[2].slice(0,12)+'…'}return o}
function art(m,c,h){var a=h%40,b=(h>>3)%30,s='';
 if(m==='sunset')s='<circle cx="'+(70+a)+'" cy="'+(105+b)+'" r="'+(30+b/3)+'" fill="#FFF3D6" opacity=".92"/><path d="M0 170 Q50 '+(120+b)+' 100 165 T200 150 V210 H0Z" fill="'+c[2]+'" opacity=".85"/><path d="M0 195 Q70 '+(160+a/2)+' 130 190 T200 180 V210 H0Z" fill="'+c[2]+'"/>';
 else if(m==='leaf'){for(var i=0;i<7;i++){var r=-60+i*20+(h>>i)%12,x=40+i*20;s+='<path d="M'+x+' 205 C'+(x-30)+' 150 '+(x-5)+' 90 '+(x+10)+' 70 C'+(x+40)+' 110 '+(x+25)+' 170 '+x+' 205Z" fill="'+(i%2?c[1]:'#fff')+'" opacity="'+(.35+i%3*.2)+'" transform="rotate('+r/4+' '+x+' 205)"/>'}}
 else if(m==='circuit'){for(var j=0;j<6;j++){var y=30+j*30+a%8;s+='<polyline points="0,'+y+' '+(40+b*2)+','+y+' '+(70+b*2)+','+(y+22)+' 200,'+(y+22)+'" fill="none" stroke="'+c[1]+'" stroke-width="2" opacity=".7"/><circle cx="'+(40+b*2)+'" cy="'+y+'" r="4" fill="'+c[2]+'"/>'}}
 else if(m==='columns'){s='<path d="M30 80 L100 40 L170 80Z" fill="'+c[2]+'"/>';for(var k=0;k<4;k++)s+='<rect x="'+(40+k*34)+'" y="88" width="16" height="'+(90+a/4)+'" fill="'+c[2]+'"/>';s+='<rect x="26" y="'+(184+a/4)+'" width="148" height="12" fill="'+c[1]+'" opacity=".6"/>'}
 else if(m==='mountain')s='<circle cx="'+(150-a)+'" cy="60" r="20" fill="#FFF3D6" opacity=".9"/><path d="M0 200 L'+(50+a)+' '+(90+b)+' L110 170 L'+(150-b)+' '+(80+a/2)+' L200 200Z" fill="'+c[1]+'"/><path d="M0 210 L70 150 L130 200 L200 140 V210Z" fill="'+c[2]+'" opacity=".85"/>';
 else if(m==='planet'){for(var n=0;n<14;n++)s+='<circle cx="'+((h>>n)*37%190+5)+'" cy="'+((h>>(n+2))*53%180+8)+'" r="'+(1+n%3*.6)+'" fill="#fff" opacity=".8"/>';s+='<circle cx="'+(90+a)+'" cy="'+(110+b)+'" r="'+(38+b/2)+'" fill="'+c[1]+'"/><ellipse cx="'+(90+a)+'" cy="'+(110+b)+'" rx="'+(66+b/2)+'" ry="14" fill="none" stroke="'+c[2]+'" stroke-width="5" transform="rotate(-18 '+(90+a)+' '+(110+b)+')"/>'}
 else if(m==='bars'){for(var q=0;q<5;q++){var hh=40+((h>>q)%90);s+='<rect x="'+(28+q*28)+'" y="'+(195-hh)+'" width="18" height="'+hh+'" fill="'+(q===4?c[2]:c[1])+'" opacity="'+(q===4?1:.85)+'"/>'}}
 else s='<circle cx="'+(150-a)+'" cy="55" r="22" fill="'+c[2]+'"/><path d="M100 190 L100 110 Q60 95 24 108 V185 Q60 172 100 190Z" fill="'+c[1]+'"/><path d="M100 190 L100 110 Q140 95 176 108 V185 Q140 172 100 190Z" fill="#fff" opacity=".85"/>';
 return s}
window.bvCoverKey=key;
window.bvCover=function(b){var k=key(b),t=T[k],h=hash((b.title||'')+(b.isbn||'')+(b.id||'')),id='g'+h,L=lines(b.title),
 ty=L.map(function(x,i){return '<text x="100" y="'+(232+i*17)+'" text-anchor="middle" font-size="15" font-weight="700" fill="#1C2430" font-family="Inter,system-ui,sans-serif">'+esc(x)+'</text>'}).join('');
 return '<svg class="cover-svg" viewBox="0 0 200 300" preserveAspectRatio="xMidYMid slice" role="img" aria-label="Cover of '+esc(b.title)+'"><defs><linearGradient id="'+id+'" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="'+t.c[0]+'"/><stop offset="1" stop-color="'+t.c[1]+'" stop-opacity=".55"/></linearGradient></defs><rect width="200" height="300" fill="url(#'+id+')"/><g>'+art(t.m,t.c,h)+'</g><rect y="206" width="200" height="94" fill="#fff" opacity=".94"/><rect y="206" width="200" height="4" fill="'+t.c[2]+'"/>'+ty+'<text x="100" y="290" text-anchor="middle" font-size="10" fill="#64748B" font-family="Inter,system-ui,sans-serif">'+esc((b.author||'').slice(0,28))+'</text></svg>'};
window.bvSearch=function(q){var i=document.getElementById('bookSearchInput');i.value=q||'';switchTab('books-tab');handleBookSearch(i.value);window.scrollTo({top:0,behavior:'smooth'})};
document.addEventListener('DOMContentLoaded',function(){var f=document.getElementById('heroSearchForm');if(f)f.addEventListener('submit',function(e){e.preventDefault();bvSearch(document.getElementById('heroSearch').value)})});
})();
