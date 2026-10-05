#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Conversor de plantillas Jinja2 (Flask) a Thymeleaf (Spring Boot) — Fit Nation.

Uso:
    python3 tools/jinja_to_thymeleaf.py [ruta/relativa/al/template ...]

Si no se pasa ninguna ruta convierte todas las que falten.
"""
import re
import sys
import pathlib

FLASK = pathlib.Path('/home/erick/Documentos/GitHub/gimnasio./app/templates')
SPRING = pathlib.Path('/home/erick/Documentos/SPRING_GYM/src/main/resources/templates')

# ---------------------------------------------------------------------------
# Mapa url_for(endpoint) -> URL (idéntico a los blueprints de Flask)
#   string          -> ruta fija
#   (ruta, [params]) -> ruta con parámetros de path
# ---------------------------------------------------------------------------
ROUTES = {
    'static': '__static__',
    'auth.dashboard': '/dashboard',
    'auth.forgot_password': '/forgot-password',
    'auth.login': '/login',
    'auth.logout': '/logout',
    'auth.reset_password': ('/reset-password/{token}', ['token']),

    'cliente_routes.index': '/clientes',
    'cliente_routes.add': '/clientes/add',
    'cliente_routes.edit': ('/clientes/edit/{id}', ['id']),
    'cliente_routes.delete': ('/clientes/delete/{id}', ['id']),
    'cliente_routes.marcar_entrada': '/clientes/asistencia/marcar-entrada',
    'cliente_routes.marcar_salida': ('/clientes/asistencia/marcar-salida/{id_asistencia}', ['id_asistencia']),
    'cliente_routes.historial': '/clientes/asistencia/historial',

    'plan_routes.index': '/planes',
    'plan_routes.add': '/planes/add',
    'plan_routes.edit': ('/planes/edit/{id}', ['id']),
    'plan_routes.delete': ('/planes/delete/{id}', ['id']),

    'inscripcion_routes.index': '/inscripciones',
    'inscripcion_routes.add': '/inscripciones/add',
    'inscripcion_routes.delete': ('/inscripciones/delete/{id}', ['id']),

    'pago_routes.index': '/pagos',
    'pago_routes.add': '/pagos/add',

    'horario_routes.index': '/horarios',
    'horario_routes.add': '/horarios/add',
    'horario_routes.edit': ('/horarios/edit/{id}', ['id']),
    'horario_routes.delete': ('/horarios/delete/{id}', ['id']),
    'horario_routes.historial': '/horarios/historial',
    'horario_routes.seleccionar': ('/horarios/seleccionar/{id}', ['id']),
    'horario_routes.deseleccionar': ('/horarios/deseleccionar/{id}', ['id']),
    'horario_routes.marcar_entrada': ('/horarios/asistencia/marcar-entrada/{id_horario}', ['id_horario']),
    'horario_routes.marcar_salida': ('/horarios/asistencia/marcar-salida/{id_asistencia}', ['id_asistencia']),

    'user.index': '/User',
    'user.add': '/User/add',
    'user.edit': ('/User/edit/{id}', ['id']),
    'user.detail': ('/User/detail/{id}', ['id']),
    'user.delete': ('/User/delete/{id}', ['id']),
}


# ---------------------------------------------------------------------------
# Utilidades de expresiones
# ---------------------------------------------------------------------------
def snake_to_camel(s):
    parts = s.split('_')
    return parts[0] + ''.join(p[:1].upper() + p[1:] for p in parts[1:])


DOTTED_CAMEL = re.compile(r'\.([a-z][a-z0-9]*(?:_[A-Za-z0-9]+)+)')


def camel_dotted(s):
    return DOTTED_CAMEL.sub(lambda m: '.' + snake_to_camel(m.group(1)), s)


PY2JAVA = {'%d': 'dd', '%m': 'MM', '%Y': 'yyyy', '%I': 'hh',
           '%M': 'mm', '%S': 'ss', '%p': 'a'}


def pyfmt(f):
    return re.sub(r'%[dmYIMSp]', lambda m: PY2JAVA[m.group(0)], f)


def norm_expr(s):
    """Traduce una expresión Jinja/Python a SpEL de Thymeleaf."""
    s = s.strip()
    # SpEL no tiene el operador `in`:  x in ['a', 'b'] -> (x == 'a' or x == 'b')
    def _in(m):
        var, items = m.group(1), [i.strip() for i in m.group(2).split(',') if i.strip()]
        return '(' + ' or '.join('%s == %s' % (var, i) for i in items) + ')'
    s = re.sub(r'([#\w][\w.]*|\([^()]*\))\s+in\s+\[([^\]]*)\]', _in, s)
    # jinja: current_user.*
    s = s.replace('current_user.is_authenticated', '#authentication.isAuthenticated')
    s = s.replace('current_user', '#authentication.principal')
    # nombre[0].upper()  ->  #strings.charAt(nombre, 0).toUpperCase()
    s = re.sub(r'([#\w][\w.]*)\[0\]\.upper\(\)',
               r'#strings.charAt(\1, 0).toUpperCase()', s)
    # duracion.seconds // 3600  ->  duracion.toHours()
    s = re.sub(r'\(\s*(\w+)\.seconds\s*%\s*3600\s*\)\s*//\s*60',
               r'(\1.toMinutes() % 60)', s)
    s = re.sub(r'\(\s*(\w+)\.seconds\s*//\s*3600\s*\)', r'(\1.toHours())', s)
    # datetime.strftime(...) -> #temporals.format(...)
    s = re.sub(r"([A-Za-z_#][\w.]*)\.strftime\('([^']*)'\)",
               lambda m: "#temporals.format(%s, '%s')" % (m.group(1), pyfmt(m.group(2))),
               s)
    # "{:,.2f}".format(x) -> #numbers.formatDecimal(...)
    s = re.sub(r'"\{:[^}]*\}"\.format\(([^)]*)\)',
               r"#numbers.formatDecimal(\1, 1, 'POINT', 2, 'COMMA')", s)
    # filtro | capitalize
    s = re.sub(r'\b([\w.#]+)\s*\|\s*capitalize\b', r'#strings.capitalize(\1)', s)
    # operador ternario de Jinja:  A if C else B  ->  (C ? A : B)
    m = re.match(r'^(.*?)\s+if\s+(.+?)\s+else\s+(.*)$', s, re.S)
    if m and '?' not in m.group(1):
        s = '(%s ? %s : %s)' % (norm_cond(m.group(2)), m.group(1).strip(), m.group(3).strip())
    # snake_case de las plantillas Flask -> camelCase de las entidades Java
    s = re.sub(r'([A-Za-z][A-Za-z0-9]*(?:_[A-Za-z0-9]+)+)',
               lambda m: snake_to_camel(m.group(1)), s)
    # x or 'defecto' -> elvis de SpEL
    s = re.sub(r"((?:[#A-Za-z_][\w.]*)(?:\([^()]*\))?)\s+or\s+('[^']*'|\"[^\"]*\")",
               r'(\1 ?: \2)', s)
    return s.strip()


_BARE = re.compile(r'^[#A-Za-z][\w.]*$')


def norm_cond(s):
    """Como norm_expr, pero garantizando un booleano (th:if / ternario)."""
    e = norm_expr(s)
    if _BARE.match(e):
        if e.endswith('isAuthenticated'):
            return e
        if e.endswith('usuariosInscritos'):
            return 'not #lists.isEmpty(%s)' % e
        return '%s != null' % e
    return e


# ---------------------------------------------------------------------------
# url_for -> marcador @@U<n>@@
# ---------------------------------------------------------------------------
def tokenize_urls(s, tokens):
    def reg(m):
        endpoint = m.group(1)
        args = dict(re.findall(r'(\w+)\s*=\s*([^,)]+)', m.group(2) or ''))
        route = ROUTES[endpoint]
        if route == '__static__':
            path = '/' + args['filename']
        elif isinstance(route, str):
            path = route
        else:
            path, params = route
            parts = []
            for p in params:
                parts.append('%s=${%s}' % (p, args[p].strip()))
            path = path + ('(' + ', '.join(parts) + ')' if parts else '')
        tokens.append(path)
        return '@@U%d@@' % (len(tokens) - 1)

    s = re.sub(r"\{\{\s*url_for\('([\w.]+)'((?:\s*,\s*[^)]+)?)\)\s*\}\}", reg, s)
    return s


def link_expr(path):
    return '@{%s}' % path


# ---------------------------------------------------------------------------
# Condicionales dentro de una etiqueta
# ---------------------------------------------------------------------------
def split_branches(body, first_cond):
    """body = 'text{% elif X %}A{% else %}B' -> [(cond, texto), ...]."""
    parts = re.split(r'\{%\s*(?:elif\s+(.*?)|else)\s*%\}', body, flags=re.S)
    branches = [(first_cond, parts[0])]
    i = 1
    while i + 1 < len(parts):
        cond = parts[i]
        branches.append((cond.strip() if cond else None, parts[i + 1]))
        i += 2
    return branches


def to_ternary(body, first_cond):
    branches = split_branches(body, first_cond)
    if branches[-1][0] is not None:      # no había {% else %}
        branches.append((None, ''))
    expr = "'%s'" % branches[-1][1].strip()
    for cond, text in reversed(branches[:-1]):
        expr = '(%s ? \'%s\' : %s)' % (norm_cond(cond), text.strip(), expr)
    return expr


def in_tag_if(s):
    """Convierte {% if %} que caen dentro de una etiqueta HTML."""
    out, i, n = [], 0, len(s)
    if_re = re.compile(r"\{%\s*if\b(.*?)%\}", re.S)
    while i < n:
        m = if_re.search(s, i)
        if not m:
            out.append(s[i:])
            break
        p = m.start()
        prev_lt = s.rfind('<', 0, p)
        prev_gt = s.rfind('>', 0, p)
        if prev_lt < 0 or prev_lt < prev_gt:
            # posición de texto: lo deja para convert_flow()
            out.append(s[i:p + 1])
            i = p + 1
            continue

        end = re.search(r'\{%\s*endif\s*%\}', s[p:])
        if not end:
            raise RuntimeError('if sin endif cerca de %d' % p)
        endif_end = p + end.end()
        body = s[m.end():p + end.start()]
        cond = m.group(1).strip()
        quotes = s.count('"', prev_lt, p)

        if quotes % 2 == 1:
            # --- Caso A: dentro del valor de un atributo ---------------
            q_open = s.rfind('"', prev_lt, p)
            eq = s.rfind('=', prev_lt, q_open)
            name_start = eq - 1
            while name_start > prev_lt and (s[name_start].isalnum()
                                            or s[name_start] in '_:-'):
                name_start -= 1
            name_start += 1
            name = s[name_start:eq].strip()
            q_close = s.find('"', endif_end)
            prefix, suffix = s[q_open + 1:p], s[endif_end:q_close]
            new_val = prefix + '${' + to_ternary(body, cond) + '}' + suffix
            out.append(s[i:name_start])
            out.append(('th:attr="%s=%s"' % (name, new_val)) if '-' in name
                       else ('th:%s="%s"' % (name, new_val)))
            i = q_close + 1
        else:
            # --- Caso B: entre atributos, contenido = nombre de atributo
            mm = re.fullmatch(r'\s*(\w+)\s*', body)
            if not mm:
                raise RuntimeError('if en etiqueta no reconocido: %r' % body)
            out.append(s[i:p])
            out.append('th:%s="${%s}"' % (mm.group(1), norm_cond(cond)))
            i = endif_end
    return ''.join(out)


# ---------------------------------------------------------------------------
# Condicionales / bucles en posición de texto
# ---------------------------------------------------------------------------
def convert_flow(s):
    out, stack, pos = [], [], 0
    pat = re.compile(r'\{%\s*(if|elif|else|endif|for|endfor)\b(.*?)%\}', re.S)
    for m in pat.finditer(s):
        out.append(s[pos:m.start()])
        kw, rest = m.group(1), m.group(2).strip()
        if kw == 'if':
            out.append('<th:block th:if="${%s}">' % norm_cond(rest))
            stack.append(('if', None))
        elif kw == 'elif':
            assert stack and stack[-1][0] == 'if'
            out.append('</th:block><th:block th:elseif="${%s}">' % norm_cond(rest))
        elif kw == 'else':
            kind, extra = stack[-1]
            if kind == 'for':
                out.append('</th:block><th:block th:if="${#lists.isEmpty(%s)}">' % extra)
            else:
                out.append('</th:block><th:block th:else>')
        elif kw == 'endif':
            out.append('</th:block>')
            stack.pop()
        elif kw == 'for':
            mm = re.match(r'(\w+)\s+in\s+(.+)', rest, re.S)
            var, lst = mm.group(1), norm_expr(mm.group(2))
            out.append('<th:block th:each="%s : ${%s}">' % (var, lst))
            stack.append(('for', lst))
        elif kw == 'endfor':
            out.append('</th:block>')
            stack.pop()
        pos = m.end()
    out.append(s[pos:])
    assert not stack, 'bloques sin cerrar: %r' % stack
    return ''.join(out)


def merge_sets(s):
    def f(m):
        return ('<th:block th:if="%s" th:with="%s=${T(java.time.Duration).between(%s, %s)}">'
                % (m.group('c'), m.group('v'), m.group('b'), m.group('a')))
    s = re.sub(r'<th:block th:if="(?P<c>[^"]*)">(?P<ws>\s*)@@SET@@(?P<v>\w+)@@(?P<a>[^@]+)@@(?P<b>[^@]+)@@',
               f, s)
    assert '@@SET@@' not in s, 'set sin asociar a un if'
    return s


def pre_sets(s):
    def f(m):
        a, b = m.group(2).strip(), m.group(3).strip()
        a = norm_expr(a)
        b = norm_expr(b)
        return '@@SET@@%s@@%s@@%s@@' % (m.group(1), a, b)
    return re.sub(r"\{%\s*set\s+(\w+)\s*=\s*([\w.]+)\s*-\s*([\w.]+)\s*%\}", f, s)


# ---------------------------------------------------------------------------
# {{ ... }}  ->  th:text
# ---------------------------------------------------------------------------
def convert_text_exprs(s):
    # 1) el contenido completo de una etiqueta es una sola expresión -> th:text
    def one(m):
        inner = m.group('inner')
        if inner.count('{{') != 1:
            return m.group(0)
        mm = re.fullmatch(r'\s*\{\{\s*(.*?)\s*\}\}\s*', inner, re.S)
        if not mm:
            return m.group(0)
        return '<%s%s th:text="${%s}"></%s>' % (m.group('tag'), m.group('attrs'),
                                                norm_expr(mm.group(1)), m.group('tag'))

    s = re.sub(r'<(?P<tag>\w+)(?P<attrs>(?:[^<>"\']|"[^"]*"|\'[^\']*\')*)>'
               r'\s*(?P<inner>\{\{.*?\}\})\s*</(?P=tag)>',
               one, s, flags=re.S)
    # 2) resto: envoltura <th:block th:text>
    s = re.sub(r'\{\{(.*?)\}\}',
               lambda m: '<th:block th:text="${%s}"></th:block>' % norm_expr(m.group(1)), s,
               flags=re.S)
    return s


# ---------------------------------------------------------------------------
# Atributos con {{ }} / @@U@@  ->  th:*
# ---------------------------------------------------------------------------
def convert_attrs(s):
    dashes = []

    def repl_tag(tm):
        attrs = tm.group(0)
        changed = False
        pend = []

        def repl_attr(am):
            nonlocal changed
            name, val = am.group(1), am.group(2)
            if '{{' not in val and '@@U' not in val:
                return am.group(0)
            changed = True
            new = re.sub(r'@@U(\d+)@@', lambda x: link_expr(SAVED[int(x.group(1))]), val)
            new = re.sub(r'\{\{\s*([^{}]+?)\s*\}\}',
                         lambda x: '${%s}' % norm_expr(x.group(1)), new)
            if '-' in name:
                pend.append((name, new))
                return ''
            return 'th:%s="%s"' % (name, new)

        attrs = re.sub(r'([a-zA-Z_][-\w:.]*)\s*=\s*"([^"]*)"', repl_attr, attrs)
        if pend:
            attrs = attrs.rstrip()
            if attrs.endswith('>'):
                attrs = attrs[:-1]
            attrs += ' th:attr="%s">' % ', '.join('%s=%s' % kv for kv in pend)
        return attrs if changed else tm.group(0)

    out = re.sub(r'<[a-zA-Z][-\w]*(?:[^<>"\']|"[^"]*"|\'[^\']*\')*>', repl_tag, s)
    return out


# ---------------------------------------------------------------------------
# Orquestación
# ---------------------------------------------------------------------------
FORM_ACTION = {
    'clientes/add.html':      '@{/clientes/add}',
    'clientes/edit.html':     '@{/clientes/edit/{id}(id=${id})}',
    'planes/add.html':        '@{/planes/add}',
    'planes/edit.html':       '@{/planes/edit/{id}(id=${id})}',
    'inscripciones/add.html': '@{/inscripciones/add}',
}

SAVED = []


def convert_body(s, tokens):
    global SAVED
    SAVED = tokens
    s = tokenize_urls(s, tokens)          # {{ url_for(...) }} -> @@U<n>@@
    s = pre_sets(s)                        # {% set v = a - b %} -> marcador
    s = in_tag_if(s)                       # {% if %} dentro de etiquetas
    s = convert_flow(s)                    # {% if/for %} en texto
    s = merge_sets(s)                      # pega th:with en el th:if anterior
    s = convert_attrs(s)                   # attr="{{ x }}" -> th:attr
    s = convert_text_exprs(s)              # {{ x }} -> th:text
    s = re.sub(r'@@U(\d+)@@', lambda m: '[[%s]]' % link_expr(tokens[int(m.group(1))]), s)
    return s


def wrap_page(body, titulo, base, page_url):
    titulo_html = convert_body(titulo, [])
    if base == 'base.html':
        layout, args = 'base :: layout', '~{:: #titulo-pagina}, ~{:: #contenido}'
    elif base == 'form-base.html':
        layout, args = 'form-base :: formLayout', '~{:: #titulo-pagina}, ~{:: #contenido}'
    elif base == 'login-base.html':
        layout, args = 'login-base :: loginLayout', '~{:: #contenido}'
    else:
        raise RuntimeError('base desconocida: %s' % base)

    titulo_frag = ('  <th:block id="titulo-pagina" th:fragment="titulo-pagina">%s</th:block>\n'
                   % titulo_html if base != 'login-base.html' else '')
    return ('<!DOCTYPE html>\n'
            '<html lang="es" xmlns:th="http://www.thymeleaf.org"\n'
            '      th:replace="~{%s(%s)}">\n\n'
            '<head>\n'
            '  <meta charset="UTF-8">\n'
            '  <title>Fit Nation</title>\n'
            '</head>\n\n'
            '<body>\n'
            '%s'
            '  <div id="contenido" th:fragment="contenido">\n'
            '%s\n'
            '  </div>\n'
            '</body>\n\n'
            '</html>\n' % (layout, args, titulo_fmt(titulo_frag), body.rstrip() + '\n'))


def titulo_fmt(t):
    return t


def convert_file(rel):
    src = (FLASK / rel)
    txt = src.read_text(encoding='utf-8')

    ext = re.search(r"\{%\s*extends\s+['\"]([^'\"]+)['\"]\s*%\}", txt)
    base = ext.group(1)
    txt = txt[ext.end():]

    m_t = re.search(r"\{%\s*block\s+titulo_pagina\s*%\}(.*?)\{%\s*endblock\s*%\}", txt, re.S)
    titulo = m_t.group(1) if m_t else ''
    if m_t:
        txt = txt[:m_t.start()] + txt[m_t.end():]

    m_c = re.search(r"\{%\s*block\s+content\s*%\}(.*?)\{%\s*endblock\s*%\}", txt, re.S)
    assert m_c, 'sin block content en %s' % rel
    contenido = m_c.group(1)
    resto = (txt[:m_c.start()] + txt[m_c.end():]).strip()
    assert not resto.strip(), 'resto no procesado en %s: %r' % (rel, resto[:200])

    tokens = []
    body = convert_body(contenido, tokens)

    # formularios sin action -> th:action explícito (además dispara el CSRF)
    if re.search(r'<form(?![^>]*\baction=)(?=[^>]*\bmethod="(?:post|POST)")', body):
        action = FORM_ACTION[rel]
        body = re.sub(r'<form(?![^>]*\baction=)([^>]*\bmethod="(?:post|POST)"[^>]*)>',
                      lambda m: '<form th:action="%s"%s' % (action, m.group(1)), body, count=1)

    out = wrap_page(body, titulo, base, rel)

    # avisos de lo que no se pudo traducir
    leftovers = []
    for pat, label in [(r'\{%', 'jinja-tag'), (r'\{\{', 'jinja-expr'),
                       (r'url_for', 'url_for'), (r'current_user', 'current_user')]:
        if re.search(pat, out):
            leftovers.append(label)
    return out, leftovers


def main():
    targets = sys.argv[1:]
    if not targets:
        targets = [str(p.relative_to(FLASK)) for p in sorted(FLASK.rglob('*.html'))]
        skip = {'base.html', 'menu.html', 'login-base.html', 'login.html',
                'dashboard.html', 'form-base.html'}
        targets = [t for t in targets if t not in skip]
    for rel in targets:
        out, leftovers = convert_file(rel)
        dst = SPRING / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(out, encoding='utf-8')
        flag = ('  <-- PENDIENTE: ' + ','.join(leftovers)) if leftovers else ''
        print('%-45s %5d líneas%s' % (rel, out.count('\n'), flag))


if __name__ == '__main__':
    main()
