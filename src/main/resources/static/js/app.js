// Función para cambiar el tema
function changeSkin() {
    const skinSelector = document.getElementById('skinSelector');
    const body = document.body;
    const selectedSkin = skinSelector.value;
    
    // Remover todas las clases de temas
    body.classList.remove('neobrutalist-theme');
    
    // Aplicar el tema seleccionado
    if (selectedSkin === 'neobrutalist') {
        body.classList.add('neobrutalist-theme');
    }
    
    // Actualizar la apariencia del selector inmediatamente
    updateSelectorAppearance(selectedSkin);
    
    // Guardar la selección en localStorage
    localStorage.setItem('selectedSkin', selectedSkin);
}

// Función para actualizar la apariencia del selector
function updateSelectorAppearance(theme) {
    const selector = document.getElementById('skinSelector');
    const selectorContainer = document.querySelector('.skin-selector');
    
    if (!selector || !selectorContainer) return;

    if (theme === 'neobrutalist') {
        // Aplicar estilos neobrutalistas inmediatamente
        selectorContainer.style.background = '#FFD93D';
        selectorContainer.style.border = '3px solid #000000';
        selectorContainer.style.borderRadius = '0';
        selectorContainer.style.boxShadow = '4px 4px 0px #000000';
        selectorContainer.style.transform = 'rotate(-1deg)';
        
        selector.style.background = '#FFFFFF';
        selector.style.border = '2px solid #000000';
        selector.style.borderRadius = '0';
        selector.style.fontWeight = 'bold';
        selector.style.color = '#000000';
        selector.style.textTransform = 'uppercase';
        selector.style.fontSize = '12px';
        selector.style.letterSpacing = '1px';
    } else {
        // Restaurar estilos elegantes
        selectorContainer.style.background = 'white';
        selectorContainer.style.border = '2px solid #000';
        selectorContainer.style.borderRadius = '8px';
        selectorContainer.style.boxShadow = '0 2px 10px rgba(0,0,0,0.1)';
        selectorContainer.style.transform = 'none';
        
        selector.style.background = '';
        selector.style.border = '1px solid #ddd';
        selector.style.borderRadius = '4px';
        selector.style.fontWeight = 'normal';
        selector.style.color = '';
        selector.style.textTransform = 'none';
        selector.style.fontSize = '14px';
        selector.style.letterSpacing = 'normal';
    }
}

// Cargar el tema guardado al cargar la página
document.addEventListener('DOMContentLoaded', function() {
    const savedSkin = localStorage.getItem('selectedSkin');
    const skinSelector = document.getElementById('skinSelector');
    
    if (skinSelector) {
        if (savedSkin) {
            skinSelector.value = savedSkin;
            changeSkin();
        } else {
            // Asegurar que el selector tenga la apariencia correcta al cargar
            updateSelectorAppearance('default');
        }
    }

    // Idioma: sincronizar con localStorage y URL
    try {
        const url = new URL(window.location.href);
        const urlLang = url.searchParams.get('lang');
        const storedLang = localStorage.getItem('preferredLang');

        if (urlLang) {
            if (storedLang !== urlLang) {
                localStorage.setItem('preferredLang', urlLang);
            }
            updateLangActive(urlLang);
        } else if (storedLang) {
            // Aplicar automáticamente el idioma preferido si la URL no lo tiene
            url.searchParams.set('lang', storedLang);
            window.location.replace(url.toString());
            return;
        } else {
            updateLangActive('es'); // coincide con default del servidor
        }
    } catch (_) {
        // Ignorar errores de URL
    }

    // Mostrar nombre de archivo seleccionado junto al botón personalizado
    const fileInput = document.getElementById('fileInput');
    const fileNameLabel = document.getElementById('fileNameLabel');
    if (fileInput && fileNameLabel) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files.length > 0) {
                fileNameLabel.textContent = this.files[0].name;
            } else {
                // Si no hay archivo, restaurar placeholder traducido si está disponible en atributo data
                const placeholder = fileNameLabel.getAttribute('data-placeholder');
                if (placeholder) fileNameLabel.textContent = placeholder;
            }
        });
        // Guardar el texto inicial como placeholder
        if (!fileNameLabel.getAttribute('data-placeholder')) {
            fileNameLabel.setAttribute('data-placeholder', fileNameLabel.textContent);
        }
    }
});

// Cambiar idioma preservando otros parámetros de la URL
function setLang(lang) {
    try {
        localStorage.setItem('preferredLang', lang);
        const url = new URL(window.location.href);
        url.searchParams.set('lang', lang);
        window.location.href = url.toString();
    } catch (e) {
        // Fallback simple
        localStorage.setItem('preferredLang', lang);
        window.location.search = '?lang=' + encodeURIComponent(lang);
    }
}

function updateLangActive(lang) {
    const buttons = document.querySelectorAll('.lang-btn');
    buttons.forEach(btn => {
        const isActive = btn.getAttribute('onclick')?.includes("'" + lang + "'");
        if (isActive) {
            btn.classList.add('active');
            btn.setAttribute('aria-pressed', 'true');
        } else {
            btn.classList.remove('active');
            btn.setAttribute('aria-pressed', 'false');
        }
    });
}

// Efectos adicionales para el tema neobrutalista
document.addEventListener('DOMContentLoaded', function() {
    // Agregar efectos de hover mejorados para elementos neobrutalistas
    const elements = document.querySelectorAll('button, .action-button, input[type="file"]');
    
    elements.forEach(element => {
        element.addEventListener('mouseenter', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = 'translate(-2px, -2px)';
            }
        });
        
        element.addEventListener('mouseleave', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = '';
            }
        });
    });
    
    // Agregar efectos hover al selector cuando esté en modo neobrutalista
    const selectorContainer = document.querySelector('.skin-selector');
    const selector = document.getElementById('skinSelector');
    
    if (selectorContainer && selector) {
        selectorContainer.addEventListener('mouseenter', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = 'rotate(-1deg) translate(-2px, -2px)';
                this.style.boxShadow = '6px 6px 0px #000000';
            }
        });
        
        selectorContainer.addEventListener('mouseleave', function() {
            if (document.body.classList.contains('neobrutalist-theme')) {
                this.style.transform = 'rotate(-1deg)';
                this.style.boxShadow = '4px 4px 0px #000000';
            }
        });
    }
});
