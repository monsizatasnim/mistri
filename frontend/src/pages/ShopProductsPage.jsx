import { useEffect, useState } from 'react';
import { apiRequest } from '../api/client';
import TextField from '../components/TextField.jsx';

const EMPTY = {
  name: '', category: '', description: '', imageUrl: '',
  availabilityType: 'SALE', salePrice: '', rentPrice: '',
};

function priceText(p) {
  const parts = [];
  if (p.salePrice != null) parts.push('Sale ' + p.salePrice);
  if (p.rentPrice != null) parts.push('Rent ' + p.rentPrice);
  return parts.join(' / ') || '-';
}

export default function ShopProductsPage() {
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiRequest('/api/shop-owner/products')
      .then((data) => setProducts(data || []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  function startEdit(p) {
    setEditingId(p.id);
    setSuccess('');
    setError('');
    setFieldErrors({});
    setForm({
      name: p.name || '',
      category: p.category || '',
      description: p.description || '',
      imageUrl: p.imageUrl || '',
      availabilityType: p.availabilityType,
      salePrice: p.salePrice != null ? String(p.salePrice) : '',
      rentPrice: p.rentPrice != null ? String(p.rentPrice) : '',
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function cancelEdit() {
    setEditingId(null);
    setForm(EMPTY);
    setFieldErrors({});
    setError('');
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setFieldErrors({});

    const type = form.availabilityType;
    const needsSale = type === 'SALE' || type === 'BOTH';
    const needsRent = type === 'RENT' || type === 'BOTH';
    const problems = {};
    if (needsSale && !(Number(form.salePrice) > 0)) problems.salePrice = 'Enter a sale price above 0';
    if (needsRent && !(Number(form.rentPrice) > 0)) problems.rentPrice = 'Enter a rent price above 0';
    if (Object.keys(problems).length > 0) {
      setFieldErrors(problems);
      setError('Please fix the highlighted fields.');
      return;
    }

    const body = {
      name: form.name.trim(),
      category: form.category.trim() || null,
      description: form.description.trim() || null,
      imageUrl: form.imageUrl.trim() || null,
      availabilityType: type,
      salePrice: needsSale ? Number(form.salePrice) : null,
      rentPrice: needsRent ? Number(form.rentPrice) : null,
    };

    setSaving(true);
    try {
      if (editingId) {
        const saved = await apiRequest('/api/shop-owner/products/' + editingId, { method: 'PUT', body });
        setProducts((list) => list.map((p) => (p.id === saved.id ? saved : p)));
        setSuccess('Product updated.');
      } else {
        const saved = await apiRequest('/api/shop-owner/products', { method: 'POST', body });
        setProducts((list) => [saved, ...list]);
        setSuccess('Product added.');
      }
      setEditingId(null);
      setForm(EMPTY);
    } catch (err) {
      setError(err.message);
      setFieldErrors(err.fieldErrors || {});
    } finally {
      setSaving(false);
    }
  }

  async function toggleStock(p) {
    setError('');
    setSuccess('');
    const next = p.stockStatus === 'AVAILABLE' ? 'OUT_OF_STOCK' : 'AVAILABLE';
    try {
      const saved = await apiRequest('/api/shop-owner/products/' + p.id + '/stock', {
        method: 'PATCH',
        body: { stockStatus: next },
      });
      setProducts((list) => list.map((x) => (x.id === saved.id ? saved : x)));
    } catch (err) {
      setError(err.message);
    }
  }

  const type = form.availabilityType;
  const showSale = type === 'SALE' || type === 'BOTH';
  const showRent = type === 'RENT' || type === 'BOTH';

  return (
    <div className="card card-wide">
      <h1>Product catalog</h1>
      <h2>{editingId ? 'Edit product #' + editingId : 'Add a product'}</h2>
      {success && <div className="form-success">{success}</div>}
      {error && <div className="form-error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="filters">
          <TextField id="name" label="Product name" value={form.name}
            onChange={handleChange} error={fieldErrors.name} />
          <TextField id="category" label="Category (optional)" value={form.category}
            onChange={handleChange} error={fieldErrors.category} />
          <div className="field">
            <label htmlFor="availabilityType">Sale or rent</label>
            <select id="availabilityType" name="availabilityType" value={type} onChange={handleChange}>
              <option value="SALE">For sale</option>
              <option value="RENT">For rent</option>
              <option value="BOTH">Sale and rent</option>
            </select>
            {fieldErrors.availabilityType && <div className="field-error">{fieldErrors.availabilityType}</div>}
          </div>
          {showSale && (
            <TextField id="salePrice" label="Sale price" type="number" min="0" step="0.01"
              value={form.salePrice} onChange={handleChange} error={fieldErrors.salePrice} />
          )}
          {showRent && (
            <TextField id="rentPrice" label="Rent price" type="number" min="0" step="0.01"
              value={form.rentPrice} onChange={handleChange} error={fieldErrors.rentPrice} />
          )}
          <TextField id="imageUrl" label="Image link (optional, starts with https://)" value={form.imageUrl}
            onChange={handleChange} error={fieldErrors.imageUrl} />
        </div>
        <TextField id="description" label="Description (optional)" value={form.description}
          onChange={handleChange} error={fieldErrors.description} />
        <div className="filter-buttons">
          <button className="btn" type="submit" disabled={saving}>
            {saving ? 'Please wait...' : editingId ? 'Save changes' : 'Add product'}
          </button>
          {editingId && (
            <button className="btn btn-grey" type="button" onClick={cancelEdit}>Cancel</button>
          )}
        </div>
      </form>

      <h2>Your products</h2>
      {loading && <p>Loading...</p>}
      {!loading && products.length === 0 && <p>No products yet. Add your first one above.</p>}
      {products.length > 0 && (
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th><th>Category</th><th>Type</th><th>Price</th><th>Stock</th><th></th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.category || '-'}</td>
                  <td>{p.availabilityType}</td>
                  <td>{priceText(p)}</td>
                  <td>
                    <span className={'badge badge-' + p.stockStatus}>
                      {p.stockStatus === 'AVAILABLE' ? 'In stock' : 'Out of stock'}
                    </span>
                  </td>
                  <td>
                    <button className="btn-small-dark" onClick={() => startEdit(p)}>Edit</button>{' '}
                    <button className="btn-small-dark" onClick={() => toggleStock(p)}>
                      {p.stockStatus === 'AVAILABLE' ? 'Mark out of stock' : 'Mark in stock'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
