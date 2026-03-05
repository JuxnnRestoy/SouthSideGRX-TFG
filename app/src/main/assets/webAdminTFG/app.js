/***********************
 * 0) Firebase init
 ***********************/
const firebaseConfig = {
  apiKey: "AIzaSyC8_lUDS8lequ7qTOH_swSTtG5K6_Mj8ZE",
  authDomain: "southside-c104b.firebaseapp.com",
  databaseURL: "https://southside-c104b-default-rtdb.firebaseio.com",
  projectId: "southside-c104b",
  storageBucket: "southside-c104b.firebasestorage.app",
  messagingSenderId: "722373261036",
  appId: "1:722373261036:web:16298d14940869456c17c9"
};
firebase.initializeApp(firebaseConfig);
const db = firebase.database();

// Estado
let cacheUsuarios = [];

// Listeners para modals (evitar duplicados)
let carritoListenerRef = null;
let comprasListenerRef = null;

// Helpers
function $(id) { return document.getElementById(id); }

function parseNum(v) {
  if (v === null || v === undefined) return 0;
  if (typeof v === "number") return v;
  if (typeof v === "string") return parseFloat(v.replace(",", ".")) || 0;
  return 0;
}

document.addEventListener("DOMContentLoaded", () => {
  // Tabs
  M.Tabs.init(document.querySelectorAll(".tabs"));
  // Modals
  M.Modal.init(document.querySelectorAll(".modal"));
  // Select
  M.FormSelect.init(document.querySelectorAll("select"));

  // Buscador
  $("buscadorUsuarios").addEventListener("input", (e) => {
    renderUsuarios(e.target.value.trim().toLowerCase());
  });

  // Abrir modal crear usuario
  $("btnAbrirCrearUsuario").addEventListener("click", () => {
    limpiarFormCrearUsuario();
    const modal = M.Modal.getInstance(document.getElementById("modalCrearUsuario"));
    modal.open();
  });

  // Crear usuario
  $("btnCrearUsuario").addEventListener("click", crearUsuarioSoloRTDB);

  // Escuchar usuarios
  escucharUsuarios();

  // Guardar edición usuario
  $("btnGuardarEdicionUsuario").addEventListener("click", guardarEdicionUsuario);
});

function escucharUsuarios() {
  $("estado").textContent = "Cargando usuarios...";

  db.ref("Usuarios").on("value", (snap) => {
    const arr = [];

    snap.forEach(ds => {
      const u = ds.val() || {};
      arr.push({
        uid: u.uid || ds.key,
        nombre: u.nombre || "",
        email: u.email || "",
        dni: u.dni || "",
        fechaNacimiento: u.fechaNacimiento || "",
        tipoUsuario: (u.tipoUsuario || "").toLowerCase() // cliente/vendedor
      });
    });

    arr.sort((a, b) => (a.nombre || "").localeCompare(b.nombre || ""));
    cacheUsuarios = arr;

    $("estado").textContent = `Usuarios: ${cacheUsuarios.length}`;
    renderUsuarios($("buscadorUsuarios").value.trim().toLowerCase());
  }, (err) => {
    console.error(err);
    $("estado").textContent = "Error cargando usuarios (mira consola).";
  });
}

function renderUsuarios(filtro) {
  const clientes = cacheUsuarios.filter(u => u.tipoUsuario === "cliente");
  const vendedores = cacheUsuarios.filter(u => u.tipoUsuario === "vendedor");

  renderLista("listaClientes", filtrarLista(clientes, filtro), "No hay clientes");
  renderLista("listaVendedores", filtrarLista(vendedores, filtro), "No hay vendedores");
}

function filtrarLista(lista, filtro) {
  if (!filtro) return lista;
  return lista.filter(u => {
    const t = `${u.nombre} ${u.email} ${u.uid} ${u.dni} ${u.fechaNacimiento}`.toLowerCase();
    return t.includes(filtro);
  });
}

function renderLista(containerId, lista, emptyMsg) {
  const cont = $(containerId);
  cont.innerHTML = "";

  if (lista.length === 0) {
    cont.innerHTML = `<p class="muted">${emptyMsg}</p>`;
    return;
  }

  const esClientes = containerId === "listaClientes";

  lista.forEach(u => {
    const col = document.createElement("div");
    col.className = "col s12 m6 l4";

    const accionesCliente = esClientes ? `
  <div class="card-action">
    <a href="#!" class="btn-small orange darken-2 editarUsuario" data-uid="${u.uid}">Editar</a>
    <a href="#!" class="btn-small blue lighten-1 verCarrito" data-uid="${u.uid}" style="margin-left:8px;">Ver carrito</a>
    <a href="#!" class="btn-small teal lighten-1 verCompras" data-uid="${u.uid}" style="margin-left:8px;">Ver compras</a>
    <a href="#!" class="btn-small red lighten-1 eliminarUsuario" data-uid="${u.uid}" style="margin-left:8px;">Eliminar</a>
  </div>
` : `
  <div class="card-action">
    <a href="#!" class="btn-small orange darken-2 editarUsuario" data-uid="${u.uid}">Editar</a>
    <a href="#!" class="btn-small red lighten-1 eliminarUsuario" data-uid="${u.uid}" style="margin-left:8px;">Eliminar</a>
  </div>
`;

    col.innerHTML = `
      <div class="card">
        <div class="card-content">
          <span class="card-title">${u.nombre || "(sin nombre)"}</span>
          <p class="muted">${u.email || "Sin email"}</p>
          <p class="muted">DNI: ${u.dni || "-"}</p>
          <p class="muted">Nacimiento: ${u.fechaNacimiento || "-"}</p>
          <p class="muted">UID: ${u.uid}</p>
        </div>
        ${accionesCliente}
      </div>
    `;

    cont.appendChild(col);
  });

  // Eventos botones
  if (esClientes) {
    cont.querySelectorAll(".verCarrito").forEach(btn => {
      btn.addEventListener("click", () => abrirCarrito(btn.dataset.uid));
    });
    cont.querySelectorAll(".verCompras").forEach(btn => {
      btn.addEventListener("click", () => abrirCompras(btn.dataset.uid));
    });
  }

  cont.querySelectorAll(".eliminarUsuario").forEach(btn => {
    btn.addEventListener("click", async () => {
      const uid = btn.dataset.uid;
      await eliminarUsuarioRTDB(uid);
    });
  });

  cont.querySelectorAll(".editarUsuario").forEach(btn => {
  btn.addEventListener("click", () => abrirEditarUsuario(btn.dataset.uid));
});
}

// ------------------ MODAL: CARRITO ------------------

function abrirCarrito(uidCliente) {
  const modal = M.Modal.getInstance(document.getElementById("modalCarrito"));
  $("carritoInfo").textContent = "Cargando...";
  $("carritoLista").innerHTML = "";
  modal.open();

  if (carritoListenerRef) carritoListenerRef.off();

  carritoListenerRef = db.ref(`Usuarios/${uidCliente}/CarritoCompras`);
  carritoListenerRef.on("value", (snap) => {
    if (!snap.exists()) {
      $("carritoInfo").textContent = "Carrito vacío.";
      $("carritoLista").innerHTML = "";
      return;
    }

    let totalGeneral = 0;
    let html = `<ul class="collection">`;

    snap.forEach(ds => {
      const v = ds.val() || {};

      const nombre = v.nombre || ds.key || "(sin nombre)";
      const cantidad = parseNum(v.cantidad);

      const precio = parseNum(v.precio);
      const precioDesc = parseNum(v.precioDesc);
      const tieneDesc = precioDesc > 0;

      const unitario = tieneDesc ? precioDesc : precio;
      const totalLinea = unitario * cantidad;

      totalGeneral += totalLinea;

      html += `
        <li class="collection-item">
          <b>${nombre}</b><br/>
          Cantidad: ${cantidad}<br/>
          Precio: ${precio.toFixed(2)} CRD
          ${tieneDesc ? `<br/>Precio desc: ${precioDesc.toFixed(2)} CRD` : ``}
          <br/>
          <b>Unitario usado:</b> ${unitario.toFixed(2)} CRD
          <br/>
          <b>Total línea:</b> ${totalLinea.toFixed(2)} CRD
        </li>
      `;
    });

    html += `</ul>`;

    $("carritoInfo").textContent =
      `Items: ${snap.numChildren()} | Total: ${totalGeneral.toFixed(2)} CRD`;

    $("carritoLista").innerHTML = html;

  }, (err) => {
    console.error(err);
    $("carritoInfo").textContent = "Error cargando carrito (mira consola).";
  });
}

// ------------------ MODAL: COMPRAS ------------------

function abrirCompras(uidCliente) {
  const modal = M.Modal.getInstance(document.getElementById("modalCompras"));
  $("comprasInfo").textContent = "Cargando...";
  $("comprasLista").innerHTML = "";
  modal.open();

  if (comprasListenerRef) comprasListenerRef.off();

  comprasListenerRef = db.ref(`Usuarios/${uidCliente}/Compras`);
  comprasListenerRef.on("value", async (snap) => {
    if (!snap.exists()) {
      $("comprasInfo").textContent = "Este usuario no tiene compras.";
      $("comprasLista").innerHTML = "";
      return;
    }

    const ids = [];
    snap.forEach(ds => ids.push(ds.key));
    $("comprasInfo").textContent = `Compras: ${ids.length}`;

    try {
      const compras = await Promise.all(
        ids.map(async (idCompra) => {
          const s = await db.ref(`Compras/${idCompra}`).once("value");
          return { idCompra, data: s.val() || {} };
        })
      );

      compras.sort((a, b) => (parseNum(b.data.fecha) - parseNum(a.data.fecha)));

      let html = `<ul class="collapsible">`;

      compras.forEach(({ idCompra, data }) => {
        const total = parseNum(data.total);
        const fecha = data.fecha ? new Date(data.fecha).toLocaleString() : "-";
        const vendedorUid = data.vendedorUid || "";
        const vendedorNombre = data.vendedorNombre || "";
        const clienteNombre = data.clienteNombre || "";

        const itemsObj = data.items || {};
        const items = Object.values(itemsObj);

        let itemsHtml = `<p class="muted">Sin items</p>`;
        if (items.length > 0) {
          itemsHtml = `<ul class="collection">`;
          items.forEach(it => {
            const nombreP = it.nombre || it.idProducto || "(sin nombre)";
            const cantidad = parseNum(it.cantidad);
            const precio = parseNum(it.precio);
            const precioDesc = parseNum(it.precioDesc);
            const precioFinal = parseNum(it.precioFinal);
            const tieneDesc = precioDesc > 0;

            itemsHtml += `
              <li class="collection-item">
                <b>${nombreP}</b><br/>
                Cantidad: ${cantidad}<br/>
                Precio: ${precio.toFixed(2)} CRD
                ${tieneDesc ? `<br/>Precio desc: ${precioDesc.toFixed(2)} CRD` : ``}
                <br/>
                <b>Precio final:</b> ${precioFinal.toFixed(2)} CRD
              </li>
            `;
          });
          itemsHtml += `</ul>`;
        }

        html += `
          <li>
            <div class="collapsible-header">
              <i class="material-icons">receipt</i>
              ${idCompra} | ${total.toFixed(2)} CRD
            </div>
            <div class="collapsible-body">
              <p><b>Fecha:</b> ${fecha}</p>
              ${clienteNombre ? `<p><b>Cliente:</b> ${clienteNombre}</p>` : ``}
              <p><b>Vendedor UID:</b> ${vendedorUid}</p>
              ${vendedorNombre ? `<p><b>Vendedor:</b> ${vendedorNombre}</p>` : ``}
              <div style="margin-top:10px;">
                <h6>Items</h6>
                ${itemsHtml}
              </div>
            </div>
          </li>
        `;
      });

      html += `</ul>`;
      $("comprasLista").innerHTML = html;

      M.Collapsible.init(document.querySelectorAll(".collapsible"));

    } catch (err) {
      console.error(err);
      $("comprasInfo").textContent = "Error cargando compras (mira consola).";
      $("comprasLista").innerHTML = "";
    }
  }, (err) => {
    console.error(err);
    $("comprasInfo").textContent = "Error cargando compras (mira consola).";
  });
}

// ------------------ CREAR USUARIO (RTDB) ------------------

function limpiarFormCrearUsuario() {
  $("nuevoNombre").value = "";
  $("nuevoEmail").value = "";
  $("nuevoDni").value = "";
  $("nuevaFechaNacimiento").value = "";
  $("nuevoTipo").value = "";
  $("crearUsuarioEstado").textContent = "";

  M.updateTextFields();
  M.FormSelect.init(document.querySelectorAll("select"));
}

async function crearUsuarioSoloRTDB() {
  const nombre = $("nuevoNombre").value.trim();
  const email = $("nuevoEmail").value.trim();
  const dni = $("nuevoDni").value.trim();
  const fechaNacimiento = $("nuevaFechaNacimiento").value; // YYYY-MM-DD
  const tipo = $("nuevoTipo").value;

  $("crearUsuarioEstado").textContent = "";

  if (!nombre) {
    $("crearUsuarioEstado").textContent = "Falta el nombre.";
    return;
  }
  if (!email) {
    $("crearUsuarioEstado").textContent = "Falta el email.";
    return;
  }
  if (!dni) {
    $("crearUsuarioEstado").textContent = "Falta el DNI.";
    return;
  }
  if (!fechaNacimiento) {
    $("crearUsuarioEstado").textContent = "Falta la fecha de nacimiento.";
    return;
  }
  if (tipo !== "cliente" && tipo !== "vendedor") {
    $("crearUsuarioEstado").textContent = "Elige el tipo de usuario.";
    return;
  }

  try {
    $("crearUsuarioEstado").textContent = "Creando ficha en Realtime Database...";

    const nuevoRef = db.ref("Usuarios").push();
    const uid = nuevoRef.key;

    const datos = {
      uid: uid,
      nombre: nombre,
      email: email,
      dni: dni,
      fechaNacimiento: fechaNacimiento,
      tipoUsuario: tipo,
      tiempoRegistro: Date.now()
    };

    await nuevoRef.set(datos);

    $("crearUsuarioEstado").textContent = "✅ Usuario creado (solo ficha RTDB).";

    setTimeout(() => {
      const modal = M.Modal.getInstance(document.getElementById("modalCrearUsuario"));
      modal.close();
    }, 600);

  } catch (e) {
    console.error(e);
    $("crearUsuarioEstado").textContent = `Error: ${e.message || e}`;
  }
}

// ------------------ ELIMINAR USUARIO (RTDB) ------------------

async function eliminarUsuarioRTDB(uid) {
  if (!uid) return;

  const ok = confirm("¿Seguro que quieres eliminar este usuario? Se borrará en Realtime Database.");
  if (!ok) return;

  try {
    await db.ref(`Usuarios/${uid}`).remove();
    M.toast({ html: "✅ Usuario eliminado" });
  } catch (e) {
    console.error(e);
    alert("Error eliminando: " + (e.message || e));
  }
}

function abrirEditarUsuario(uid){
  const u = cacheUsuarios.find(x => x.uid === uid);
  if(!u){
    alert("No se encontró el usuario en cache.");
    return;
  }

  $("editarUsuarioEstado").textContent = "";
  $("editUid").value = uid;

  $("editNombre").value = u.nombre || "";
  $("editEmail").value = u.email || "";
  $("editDni").value = u.dni || "";
  $("editFechaNacimiento").value = u.fechaNacimiento || "";
  $("editTipo").value = (u.tipoUsuario || "cliente");

  // refrescar labels + select
  M.updateTextFields();
  M.FormSelect.init(document.querySelectorAll("select"));

  const modal = M.Modal.getInstance(document.getElementById("modalEditarUsuario"));
  modal.open();
}

async function guardarEdicionUsuario(){
  const uid = $("editUid").value;
  const nombre = $("editNombre").value.trim();
  const email = $("editEmail").value.trim();
  const dni = $("editDni").value.trim();
  const fechaNacimiento = $("editFechaNacimiento").value;
  const tipo = $("editTipo").value;

  $("editarUsuarioEstado").textContent = "";

  if(!uid){
    $("editarUsuarioEstado").textContent = "UID inválido.";
    return;
  }
  if(!nombre){
    $("editarUsuarioEstado").textContent = "Falta el nombre.";
    return;
  }
  if(!email){
    $("editarUsuarioEstado").textContent = "Falta el email.";
    return;
  }
  if(!dni){
    $("editarUsuarioEstado").textContent = "Falta el DNI.";
    return;
  }
  if(!fechaNacimiento){
    $("editarUsuarioEstado").textContent = "Falta la fecha de nacimiento.";
    return;
  }
  if(tipo !== "cliente" && tipo !== "vendedor"){
    $("editarUsuarioEstado").textContent = "Tipo inválido.";
    return;
  }

  try{
    $("editarUsuarioEstado").textContent = "Guardando...";

    const updates = {
      nombre,
      email,
      dni,
      fechaNacimiento,
      tipoUsuario: tipo
    };

    await db.ref(`Usuarios/${uid}`).update(updates);

    $("editarUsuarioEstado").textContent = "✅ Guardado";

    setTimeout(() => {
      const modal = M.Modal.getInstance(document.getElementById("modalEditarUsuario"));
      modal.close();
    }, 500);

  }catch(e){
    console.error(e);
    $("editarUsuarioEstado").textContent = `Error: ${e.message || e}`;
  }
}